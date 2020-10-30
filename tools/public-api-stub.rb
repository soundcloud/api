require 'digest'
require 'sinatra'

set :bind, '0.0.0.0'

post '/oauth2/token' do
  content_type 'application/json'
  '{"OK"}'
end

get '/tracks/:id' do
  content_type 'application/json'
  '{"id":12345}'
end

get '/-/health' do
  # Kristof told me to write this
  'Okey dokey'
end

get '/i1/tracks/:id/streams' do
  content_type 'application/json'
  '{}'
end
