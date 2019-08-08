from subprocess import run, PIPE
from collections import namedtuple
from hashlib import md5
import unittest
import random
import string
import tempfile
import os
import assertions


# Integration test the pure-proxy mode
class TestIntegrationNoS3(unittest.TestCase, assertions.Assertions):

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

    def test_chunk_large(self):
        asset = self.assets['10mb']
        cmd = '''
            curl --verbose \
                -o /dev/null \
                -H "Host: api.sc.local" \
                -H "Transfer-Encoding: chunked" \
                -F "track[asset_data]=@{};filename=test_chunk_large.wav" \
                -F "track[title]=123" \
                -F "oauth_token=s3cr3t_2" \
                asset_uploads/tracks
            '''.format(asset.name)
        res = run(cmd, shell=True, stderr=PIPE, stdout=PIPE).stderr.decode('ascii', 'ignore')
        self.assertRequestEntityTooLarge(res)

if __name__ == '__main__':
    unittest.main()
