require 'digest'
require 'sinatra'

set :bind, '0.0.0.0'

def calc_result_from_io(io)
  digest = Digest::SHA1.new
  length = 0
  num_slices = 0

  buf = ''
  loop do
    res = io.read(1024, buf)
    break if res.nil?

    puts "Read #{length} bytes" if (num_slices % 10000) == 0
    num_slices += 1

    digest << buf
    length += buf.length
  end

  "ok #{length} #{digest}"
end

post '/tracks' do
  calc_result_from_io(params['track']['asset_data'][:tempfile])
end

post '/oauth2/token' do
  content_type 'application/json'
  '{"OK"}'
end

get '/tracks/:id' do
  content_type 'application/json'
  '{}'
end

get '/-/health' do
  # Kristof told me to write this
  'Okey dokey'
end

get '/i1/tracks/:id/streams' do
  content_type 'application/json'
  '{}'
end
