from subprocess import check_output, STDOUT
from collections import namedtuple
import unittest
import string
import tempfile
import os
import assertions


class TestIntegration(unittest.TestCase, assertions.Assertions):
    def test_invalid_method(self):
        cmd = '''
            curl --verbose \
                -H "Host: api.sc.local" \
                asset_uploads/tracks
        '''
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertRequest(res, 'GET', '/tracks')
        self.assertMisdirected(res)
        self.assertNoStore(res)

    def test_invalid_host(self):
        cmd = '''
            curl --verbose \
                -X POST    \
                -H "Host: xapi.sc.local" \
                asset_uploads/tracks
        '''
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertRequest(res, 'POST', '/tracks')
        self.assertMisdirected(res)

    def test_bad_request(self):
        cmd = '''
            curl --verbose \
                -d "some post data" \
                -H "Host: api.sc.local" \
                -H "Content-Type: multipart/form-data; boundary=------------------------2bd6642950bb9c00" \
                asset_uploads/tracks/2/comments
        '''
        res = check_output(cmd, shell=True, stderr=STDOUT).decode('ascii')
        self.assertBadRequest(res)
