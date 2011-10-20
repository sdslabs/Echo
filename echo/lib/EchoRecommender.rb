require 'sinatra'
require 'mongo'
require 'haml'

class EchoRecommender
  attr_accessor :uuid, :db
  def getSimilarDocument(uuid)

   return uuid

  end
end
if __FILE__ == $0

    reco =EchoRecommender.new
    reco.db= "mongodbofcourse"
    p reco.getSimilarDocument("hello")
    p reco.db
  
end

