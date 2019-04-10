from re import search


class Assertions:
    '''Helpers to assert on responses/request dumps from echo_service.go.'''

    def assertAuthorization(self, request_dump, token):
        if not search(r'Authorization: OAuth {}\r\n'.format(token),
                      request_dump):
            raise AssertionError('Expected Authorization header: ' +
                                 request_dump)

    def assertChecksum(self, request_dump, checksum):
        if not search(r'X-Track-Asset-Md5: {}\r\n'.format(checksum),
                      request_dump):
            raise AssertionError('Expected checksum header: ' + request_dump)

    def assertRequest(self, request_dump, method, path):
        if not search(r'{} {} HTTP/1.1\r\n'.format(method, path), request_dump):
            raise AssertionError('Expected {} request to {}: {}'.format(
                method, path, request_dump))

    def assertRequestEntityTooLarge(self, request_dump):
        if not search(r'HTTP/1.1 413 Request Entity Too Large', request_dump):
            raise AssertionError('Expected request to be too large: ' +
                                 request_dump)

    def assertNoTrackAssetData(self, request_dump):
        if search(r'Content-Disposition: form-data; name="track\[asset_data\]"',
                  request_dump):
            raise AssertionError('Expected no track[asset_data]:' +
                                 request_dump)

    def assertTrackOriginalFilename(self, request_dump, filename):
        if not search(
                r'Content-Disposition: form-data; name="track\[original_filename\]"\r\n\r\n{}\r\n'
                .format(filename), request_dump):
            raise AssertionError('Expected track[original_filename]')

    def assertTrackUID(self, request_dump):
        header = search(
            r'X-Track-Asset-Location:.*\.amazonaws\.com/(?P<uid>[0-9a-zA-Z_-]{12})\r\n',
            request_dump)
        if not header:
            raise AssertionError('Expected X-Track-Asset-Location: ' +
                                 request_dump)

        form_data = search(
            r'Content-Disposition: form-data; name="track\[uid\]"\r\n\r\n(?P<uid>[0-9a-zA-Z_-]{12})\r\n',
            request_dump)
        if not form_data:
            raise AssertionError('Expected track[uid]: ' + request_dump)

        if header.group('uid') != form_data.group('uid'):
            raise AssertionError('Expected UID to match: ' + request_dump)
