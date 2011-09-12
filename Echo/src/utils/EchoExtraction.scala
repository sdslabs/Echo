package sdslabs.practice

import com.mongodb._
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata._
import org.apache.tika.metadata.Metadata._
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import java.net.URL
import java.io.DataInputStream
import java.net.URLConnection
import org.w3c.dom._
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.ParserConfigurationException
import scala.collection.mutable._
import java.nio.charset.Charset
import org.json.simple.JSONValue
import org.json.simple.JSONObject
import org.json.simple.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.NullPointerException
import scala.collection.JavaConversions._

class EchoExtraction {
  
  def getTitle( f : File ): String = {	
		var title_field : String = DublinCore.TITLE		
		var stream : InputStream = new FileInputStream(f)            // converting from file to InputStream
		var parser : Parser = new AutoDetectParser()                // to auto detect parser
		var handler : ContentHandler = new BodyContentHandler(-1)      // in argument, maximam limit has to be specified
		var context : ParseContext = new ParseContext() 	
		context.set(classOf[Parser], parser)		
		var metadata : Metadata = new Metadata()     
		try {
			parser.parse(stream, handler, metadata, context)
		} 
		finally {
			stream.close()
		}
		var title : String = new String("")
		metadata.names foreach ( name => {
			if(name.compareTo(title_field) == 0 )    {
				title = metadata.get(name)
			}  
		})		
		return title.toLowerCase()
  }
  
  def searchGoogle(str : String) : Map[String, String] = {
    var query : String = str.replaceAll(" ", "+").replaceAll(".pdf", "")
    var map : Map[String, String] = new HashMap[String, String]()
    try {
      var url : String = "https://www.googleapis.com/books/v1/volumes?q=" + query
      var sb : StringBuilder = getJSONString(url)
      var json : JSONObject = JSONValue.parse(sb.toString()).asInstanceOf[JSONObject]
      var item : String = json.get("items").toString
	  var json2 : JSONArray = JSONValue.parse(item).asInstanceOf[JSONArray]
	  var book = json2.get(0).toString // taking only one book
      var json3 : JSONObject = JSONValue.parse(book).asInstanceOf[JSONObject]     // all content of first book
      var selflink : String = json3.get("selfLink").toString()
      var sb1 : StringBuilder = getJSONString(selflink)
      var jsonBook : JSONObject = JSONValue.parse(sb1.toString()).asInstanceOf[JSONObject]   // JSON file of the book
      map = extractJSON(jsonBook)     
    }    
    return map
  }
  
  
  def extractJSON(json : JSONObject) : Map[String, String] = {
    var map : java.util.Map[String, String] = new java.util.HashMap[String, String]()
    var str : String = json.get("volumeInfo").toString()
    var json1 : JSONObject = JSONValue.parse(str).asInstanceOf[JSONObject]
    var keyset : java.util.Set[String] = json1.keySet().asInstanceOf[java.util.Set[String]]
    var iter : java.util.Iterator[String] = keyset.iterator()
    while(iter.hasNext()) {
      var tempKey : String = iter.next()
      if (tempKey != "imageLinks") {
        map.put(tempKey, json1.get(tempKey).toString())
      } else {
        var tempJsonString : String = json1.get(tempKey).toString()
        var tempJson : JSONObject = JSONValue.parse(tempJsonString).asInstanceOf[JSONObject]
        var tempKeySet : java.util.Set[String] = tempJson.keySet().asInstanceOf[java.util.Set[String]]
        var tempIter : java.util.Iterator[String] = tempKeySet.iterator()
        while(tempIter.hasNext()) {
          var tempTempKey : String = tempIter.next()
          map.put(tempTempKey, tempJson.get(tempTempKey).toString())
        }
      }
    }
    var finalMap : Map[String, String] = map
    return finalMap
  }
  
  
  def getJSONString(url : String) : StringBuilder = {
    var is : InputStream = new URL(url).openStream()
	var rd : BufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))
	var sb : StringBuilder = new StringBuilder()
	var cp : Int = 0
	cp = rd.read()
	while (cp != -1) {
	     sb.append(cp.asInstanceOf[Char])
	     cp = rd.read()
	}
    return sb
  }
}