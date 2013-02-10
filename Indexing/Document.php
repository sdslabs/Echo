<?php
require("init.php");

/*
One instance of this class is to be created for a single Document. SolariumDocument is the document object that can be indexed in solarium. 
*/
class Document
{
	private $SolariumDocument;
	private $filePath;
	private $fileName;
	private $fileExt;
	private $isbnPDF;

	public $indexed = false;

	public function __construct($file)
	{
		preg_match("/[^\.\/]*\./", $file, $this->fileName); //Extracting the file name, path and extension 
		preg_match("/\.[a-z]*/i", $file, $this->fileExt);
		preg_match("/.*[a-z]*\//i", $file, $this->filePath);

		$a = array("title", "author");
		$b =  array("aa", "bb");
		$this->SolariumDocument = new Solarium_Document_ReadWrite();

		$this->filePath = $this->filePath[0]; //Used since preg_match returns an array
		$this->fileExt = $this->fileExt[0];
		$this->fileName = substr($this->fileName[0], 0, -1); //To remove the dot at the end. I suck at regex
				//echo $fileExt . " " . $fileName . " " . $filePath;

		/*this->$fileExt = substr(this->fileName, strpos(this->$fileName[0], '.'));
		this->$fileName = substr(this->$fileName[0], 1, -this->$fileExt.length);
		this->$filePath = substr($file, )*/
	}

	public function showData()
	{
		foreach ($this->SolariumDocument as $key => $value) 
		{
			if(is_array($this->SolariumDocument->$key))
				foreach ($this->SolariumDocument->$key as $field => $val)
					echo $field . " : " . $val . "<br>";
			else
				echo $key . " : " . $value . "<br>";

		} 
		echo "<hr>";

		//echo $this->fileName;
	}

	function getIsbnPDF() //Returns 0 if isbn for the pdf doesnt exist
	{
		if(isset($this->isbnPDF)) 
			return $this->isbnPDF;

		else //Sets the isbn if function is called for the first time
		{

			$command = "pdftotext ". $this->filePath. str_replace(" ", "\ ", $this->fileName). $this->fileExt. " -| grep -iE 'isbn.*[.\s]*[0-9\-]{13,17}' ";
			//echo $command;
			$this->isbnPDF = shell_exec($command);
			//echo $this->isbn;
				
			if(!preg_match("/[0-9\-]{13,17}/" ,$this->isbnPDF, $this->isbnPDF))
			{
				$this->isbnPDF = 0;
			}

			else
			{	
				$this->isbnPDF = str_replace('-', '', $this->isbnPDF[0]);
				//echo  "\t" . " 	:  " . $rhis->fileName . $this->isbn[0]. "<br>";
				//$found++;
			}
		return $this->isbnPDF;
		}
	}

	public function addMetadata() //Solr schema has to be modified to accomodate certain fields.
	{
		$json = array();

		if($this->getIsbnPDF())
			$query = "isbn:".$this->isbnPDF;
		else
			$query = $this->fileName;

		$query = str_replace(' ', '%20', $query);
		$json=json_decode(file_get_contents("https://www.googleapis.com/books/v1/volumes?q=".$query."&key=AIzaSyC-CRDiAPdXEBHgvM2YF-x6manmYk9H6D4"), true);	
		//print_r($json);

		if(!empty($json))
		{
			$this->SolariumDocument->title = $json["items"][0]["volumeInfo"]["title"];
			$this->SolariumDocument->author = $json["items"][0]["volumeInfo"]["authors"][0];
			$this->SolariumDocument->description = $json["items"][0]["volumeInfo"]["description"];
			$this->SolariumDocument->isbn_10 = $json["items"][0]["volumeInfo"]["industryIdentifiers"][0]["identifier"] ;
			$this->SolariumDocument->isbn_13 = $json["items"][0]["volumeInfo"]["industryIdentifiers"][1]["identifier"];
			$this->SolariumDocument->imageLink = $json["items"][0]["volumeInfo"]["imageLinks"]["smallThumbnail"];
			$this->SolariumDocument->selfLink = $json["items"][0]["selfLink"];	
		}
		echo isset($this->SolariumDocument->description);

		$this->setOtherFields();
	}

	private function getUnsetFields()
	{
		$unsetFields = array();
		foreach ($this->SolariumDocument as $key => $value) 
		{				
			if(!$this->SolariumDocument->$key)
				{
					echo $key . " : ". $value . "<br>";
					//echo $this->SolariumDocument->$key . "<br>";
					//var_dump(isset($this->SolariumDocument->$key));//	 "<br>";
				}
		} 
		return $unsetFields;
	}

	private function setOtherFields()
	{
		$json = array();
		$category = array();
		$unsetFields = $this->getUnsetFields();

		if($this->SolariumDocument->selfLink)
		{
			$json=json_decode(file_get_contents($this->SolariumDocument->selfLink), true);	
			$this->SolariumDocument->categories = $json["volumeInfo"]["categories"][0];
			$this->SolariumDocument->categories = preg_split("/[\/&,]+/", $this->SolariumDocument->categories);

			for($i = 0; $i < sizeof($unsetFields); $i++)
			{
				$field = $unsetFields[$i];
				$this->SolariumDocument->$field = $json["volumeInfo"][$field];
			}

			return 1;
		}
		else
		{
			return 0;

		}
	}

	public function addContent()
	{
		$command = "pdftotext ". $this->filePath. str_replace(" ", "\ ", $this->fileName). $this->fileExt;
		
	
		shell_exec($command);
		$this->SolariumDocument->content = preg_replace('/[\x00-\x09\x0B\x0C\x0E-\x1F\x7F]/', '', file_get_contents($this->filePath.$this->fileName.".txt")); //For replacing non-printable characters
		shell_exec("rm ".$this->filePath.str_replace(" ", "\ ", $this->fileName).".txt");

	}

	public function validateDoc()
	{
		 //echo "its here" . isset($this->SolariumDocument->isbn_10) . '<br>';
		 return $this->SolariumDocument->isbn_10 ? true : false;
	}
	
	public function getSolariumDocument()
	{
		return $this->SolariumDocument;
	}

}


?>