require 'sinatra'
require 'mongo'
require 'mongo_mapper'


module Echo
    class Book
        include MongoMapper::Document 
    
        MongoMapper.connection = Mongo::Connection.new('http://localhost',27017)
        MongoMapper.database='echo'
      options.each_pair{|key, value| instance_variable_set("@#{key}",value) if self.respond_to? key}
    

