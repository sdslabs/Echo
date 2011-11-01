require 'haml'
require 'sinatra'
require 'json'
require 'rest_client'
require 'mongo_mapper'
require 'mongo'

ECHO_ROOT= File.join(File.expand_path(File.dirname(__FILE__)), '..') unless defined?(ECHO_ROOT)

#--Configuration ------------
configure do

    set :root, File.dirname(__FILE__)
    set :app_file, __FILE__
    set :views, Proc.new {File.join(root, "views")}
    set :show_exceptions, true
    set :dump_errors, true
end

#-----config ends----------

#---routing starts

get '/' do
  settings.log.info('Echo Sinatra server is up and running!')
  Welcome to Echo!
  haml :home
end
get '/category/?query' do
    connection = Mongo::Connection.new('localhost')
      db = connection.db("mydb")
      coll = db.collection("testCollection")
      result = []
      counter = 0
	tempquery = params[:query]
	temp = tempquery.split("+")
	temp.each { |query|
		s = query.length
		n = s
		while n > 2 
			(1..s-n+1).each { |m|
				tempQuery = query[m-1, n]
				cursor = coll.find({"id" => tempQuery})
				if cursor.has_next?
					doc = cursor.next
					result = result | doc["list"]
					counter += 1
				end
			}
		if counter > 0
			break
		end
		n = n-1
		end
	}
      if result.empty? == false
      haml :category, :locals => { :results => result}
      else
      "Sorry ! No Results Found."
      end
end

def getResults(res)
  db = Mongo::Connection.new('127.0.0.1',27017).db('echo')
  @book = db.collection('book')
  settings.log.info('Connected to MongoDB')
  $results =[]
  bookResults=[]
  sResults=JSON.parse(res.to_json)
  sResults.each{ |book| $results.push(Array(book.find("id" => book['id'])))}
  $results.each { |book|
    bookResults.push(Array({
      "title"=>book['title'],
      "authors"=>book['authors'],
      "publisher"=>book['publisher'],
      "categories"=>book['categories'],
    }))}
  return bookResults
  
  
end
get '/search/?' do
  query= params[:query]
  settings.log.info('echo got your search term:' + query)
  raise ArgumentError, 'Give the search query after /search/' if query.nil?
  url='http://localhost:8080/search/?query='+ query
  $res = RestClient.get url , :content_type => json, :accept => json
  searchResults=getResults($res)
  bid = $searchResults[0]['id']
  url1='http://localhost:8080/search/?rec='+ bid
  $res1 = RestClient.get url1, :content_type => json, :accept => json
  recResults = getResults($res1)
  if bookResults.nil? settings.log.info('Error retrieving results from Echo Server ...')
  
  else
    settings.log.info('Retrieved results from Echo Server')
    haml :results, :locals => {:results =>searchResults, :recResults => recResults}
  end
end



  
  

