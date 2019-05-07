from subprocess import check_output, STDOUT
from collections import namedtuple
from hashlib import md5
import unittest
import random
import string
import tempfile
import os
import assertions


class TestIntegration(unittest.TestCase, assertions.Assertions):

    @classmethod
    def setUpClass(cls):
        '''Creates files with random data to be used in tests.'''
        cls.assets = {}
        Asset = namedtuple('Asset', ['name', 'md5'])
        for mnemonic, size_bytes in [('1mb', 1 << (10 * 2)),
                                     ('10mb', 10 << (10 * 2))]:
            with tempfile.NamedTemporaryFile(delete=False) as tmp:
                random_data = os.urandom(size_bytes)
                checksum = md5(random_data).hexdigest()
                tmp.write(random_data)
                cls.assets[mnemonic] = Asset(tmp.name, checksum)

    @classmethod
    def tearDownClass(cls):
        '''Cleans up any random data files that were created for tests.'''
        for _name, tmp in cls.assets.items():
            os.unlink(tmp.name)

    def test_chunk(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --fail --verbose \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk.wav" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_1" \
                asset_uploads/tracks
        '''.format(asset.name)
        res = check_output(cmd, shell=True).decode('ascii')
        self.assertAuthorization(res, 's3cr3t_1')
        self.assertChecksum(res, asset.md5)
        self.assertNoTrackAssetData(res)
        self.assertRequest(res, 'POST', '/tracks')
        self.assertTrackOriginalFilename(res, 'test_chunk.wav')
        self.assertTrackUID(res)

    def test_chunk_large(self):
        asset = self.assets['10mb']
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk_large.wav" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_2" \
                asset_uploads/tracks
            '''.format(asset.name)
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertRequestEntityTooLarge(res)

    def test_length(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --fail --verbose \
                -H "Host: api.sc.local" \
                -F "track[asset_data]=@{};filename=test_length.wav" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_3" \
                asset_uploads/tracks
            '''.format(asset.name)
        res = check_output(cmd, shell=True).decode('ascii')
        self.assertAuthorization(res, 's3cr3t_3')
        self.assertChecksum(res, asset.md5)
        self.assertNoTrackAssetData(res)
        self.assertRequest(res, 'POST', '/tracks')
        self.assertTrackOriginalFilename(res, 'test_length.wav')
        self.assertTrackUID(res)

    def test_length_large(self):
        asset = self.assets['10mb']
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                -F "track[asset_data]=@{};filename=test_length_large.wav" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_4" \
                asset_uploads/tracks
            '''.format(asset.name)
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertRequestEntityTooLarge(res)

    def test_large_token(self):
        token_bytes = 256
        token = ''.join(
            random.choices(
                string.ascii_uppercase + string.digits, k=token_bytes))
        cmd = '''
            curl --fail --verbose \
                -H "Host: api.sc.local" \
                -F "oauth_token={}" \
                asset_uploads/tracks
            '''.format(token)
        res = check_output(cmd, shell=True).decode('ascii')
        self.assertRequest(res, 'POST', '/tracks')
        self.assertAuthorization(res, token[0:64])


if __name__ == '__main__':
    unittest.main()
