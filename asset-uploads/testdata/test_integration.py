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
        self.assertRequest(res, 'POST', '/tracks-after-upload')
        self.assertTrackOriginalFilename(res, 'test_chunk.wav')
        self.assertTrackAssetLocation(res)
        self.assertTrackUID(res)

    def test_chunk_for_no_upload_features_user(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk.wav" \
                -F "track[title]=123" \
                -F "oauth_token=valid-token-no-features-user" \
                asset_uploads/tracks
        '''.format(asset.name)
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertUnauthorizedRequest(res)

    def test_invalid_field_provided(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk.wav" \
                -F "track[title]=123" \
                -F "track[uid]=should-not-be-here" \
                -F "oauth_token=valid-token-no-features-user" \
                asset_uploads/tracks
        '''.format(asset.name)
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertBadRequest(res)

    def test_alternate_routes(self):
        paths = ["/v1/tracks/", "/tracks", "/tracks.json",
                "/tracks.json/", "/users/123/tracks", "/tracks/2",
                "/tracks/2.json", "/me/tracks", "/me/tracks.json"]
        for path in paths:
            asset = self.assets['1mb']
            cmd = '''
                curl --fail --verbose \
                    -H "Host: api.sc.local" \
                    -H "Transfer-Encoding: chunked" \
                    -F "track[asset_data]=@{};filename=test_chunk.wav" \
                    -F "track[title]=123" \
                    -F "oauth_token=s3cr3t_1" \
                    asset_uploads{}
            '''.format(asset.name, path)
            res = check_output(cmd, shell=True).decode('ascii')
            self.assertAuthorization(res, 's3cr3t_1')
            self.assertChecksum(res, asset.md5)
            self.assertNoTrackAssetData(res)
            self.assertRequest(res, 'POST', '/tracks-after-upload')
            self.assertTrackOriginalFilename(res, 'test_chunk.wav')
            self.assertTrackAssetLocation(res)
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
        self.assertRequest(res, 'POST', '/tracks-after-upload')
        self.assertTrackOriginalFilename(res, 'test_length.wav')
        self.assertTrackAssetLocation(res)
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
        token_bytes = 2048
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
        self.assertRequest(res, 'POST', '/tracks-after-upload')
        self.assertAuthorization(res, token[0:1024])

    def test_empty_filename(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --fail --verbose \
                -H "Host: api.sc.local" \
                -F "track[asset_data]=@{};filename=" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_5" \
                asset_uploads/tracks
            '''.format(asset.name)
        res = check_output(cmd, shell=True).decode('ascii')
        self.assertAuthorization(res, 's3cr3t_5')
        self.assertChecksum(res, asset.md5)
        self.assertNoTrackAssetData(res)
        self.assertRequest(res, 'POST', '/tracks-after-upload')
        self.assertTrackOriginalFilename(res, '')
        self.assertTrackAssetLocation(res)
        self.assertTrackUID(res)

    def test_chunk_with_bad_token(self):
        asset = self.assets['1mb']
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk.wav" \
                -F "track[title]=123" \
                -F "oauth_token=bad-token" \
                asset_uploads/tracks
        '''.format(asset.name)
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertUnauthorizedRequest(res)

    def test_generic(self):
        token = '04u7h-t0k3n'
        cmd = '''
            curl --fail --verbose \
                -H "Host: api.sc.local" \
                -F "oauth_token={}" \
                asset_uploads/some-endpoint
            '''.format(token)
        res = check_output(cmd, shell=True).decode('ascii')
        self.assertRequest(res, 'POST', '/tracks-after-upload')
        self.assertAuthorization(res, token)


if __name__ == '__main__':
    unittest.main()
