<?php

require('getDocument.php');
require('getCategory.php');
require('Document.php');
//require('init.php');


//$path = '/home/jayant/eBooks/';
//$files = glob($path.'*.pdf');
//$docs = array();
header('content-type:text/html;charset=utf-8');

//echo $files[0];
 //$files[0] = str_replace(' ', '\ ', $files[0]);

class Indexer
{
	private $path = "/home/jayant/eBooks/";
	private $files = array();
	private $docs = array();
	private $numberIndexed = 0;

	public function __construct($path)
	{
		$this->path = $path;
		$this->files = glob($path.'*.pdf');
		//print_r($this->files);
	}

	public function showFiles()
	{
		echo "wtf?";
		for($i = 0; $i < sizeof($this->files); $i++)
			{
				echo $i. " : ".$this->files[$i]."<br>";
		//		echo "weird";
			}
	}

	public function addFiles($path)
	{
		$files = glob($path.'*.pdf');
		echo "called" . sizeof($files);
		for($i = 0; $i < sizeof($files); $i++)
			array_push($this->files, $files[$i]);
//the thing is that the docs indexed stay indexed. so dont index all docs again. keep track of number indexed and index the newer ones. 
		//also, addfiles is for adding new files, after which adddocs is to be called to create docs from files for the NEW files only. change adddocs accordingly
		//copy elements of $files to $this->files

	}

	public function addDocs()
	{
		for($i = sizeof($this->docs); $i < 20; $i++)
		{
			$this->docs[$i] = new Document($this->files[$i]);
			$this->docs[$i]->addMetadata();
			$this->docs[$i]->showData();
			$this->docs[$i]->addContent();
			//array_push($this->docs, $this->docs[$i]->getSolariumDocument());
		}
	}

	public function indexDocs()
	{
		global $config;
		$client = new Solarium_Client($config);
		$update = $client->createUpdate();

		$docs = array();
		for($i = $this->numberIndexed; $i < sizeof($this->docs); $i++)
			if($this->docs[$i]->validateDoc()) 
				$docs[$i] = $this->docs[$i]->getSolariumDocument();
			else
				echo "NOT SET!" . $i;

		$update->addDocuments($docs);
		$update->addCommit();
		$result = $client->update($update);


		if(true)//check for result of update query. 
		{
			for($i = $this->numberIndexed; $i < sizeof($this->docs); $i++)
				$this->docs[$i]->indexed = true;
			$this->numberIndexed = sizeof($this->docs);
		}
		echo '<b>Update query executed</b><br/>';
		echo 'Query status: ' . $result->getStatus(). '<br/>';
		echo 'Query time: ' . $result->getQueryTime() . '<br>';
		echo sizeof($docs) . sizeof($this->docs).'<br>';
	}	

}

$ind = new Indexer("/home/jayant/eBooks/");
//$ind->addFiles("/home/jayant/Books/");
$ind->addDocs();
$ind->indexDocs();
$ind->showFiles();

$client = new Solarium_Client($config);
$query = new Solarium_Query_Update;
$query = $client->createSelect();

// this executes the query and returns the result
$resultset = $client->select($query);

// display the total number of documents found by solr
echo '<br>'. 'NumFound: '.$resultset->getNumFound();

/*$command = "pdftotext ". $files[0]. " -| grep \"ISBN\"";
//	echo $command;
	//$isbn = shell_exec($command);
	//echo $isbn;
	$not = 0; $found = 0;
for($i = 0; $i <100; $i++)
{
	$fileName = substr($files[$i], 20, -4);

	$files[$i] = str_replace(' ', '\ ', $files[$i]);

//preg_match("/[0-9\-]{13,}/" ,$matches[0], $match);
//print_r($match);

	$isbn = shell_exec("pdftotext ". $files[$i]. " -| grep -iE 'isbn.*[.\s]*[0-9\-]{13,17}' ");
	echo $isbn[0];

	if(!preg_match("/[0-9\-]{13,17}/" ,$isbn, $isbn))
	{
		echo $i." \t" .$fileName ." : not found ". $isbn." <br>" ;
		$not++;
	}

	else
	{	
		$isbn = str_replace('-', '', $isbn);
		echo $i. "\t" . " 	:  " . $fileName . $isbn[0]. "<br>";
		$found++;
	}
	$fileName = preg_replace('/[^a-z0-9]/i', '%20', $fileName);
	//$docs[$i] = getDocument($fileName);
	//$docs[$i]["categories"] = getCategory($docs[$i]);
}
echo $not."/".($found+$not)	;
/*
for($i = 0; $i < 6; $i++)
{
	foreach ($docs[$i] as $key => $value) 
	{
		if(is_array($docs[$i][$key]))
			foreach ($docs[$i][$key] as $key2 => $value) {
				echo $key2. " : ". $value . "<br>";
			}
		else
			echo $key. " : ". $value . "<br>";
	}

	echo "<hr>";
}
/*
$client = new Solarium_Client($config);
$update = $client-createUpdate();
$update->addDocuments($docs);
$update->addCommit();
$result = $client->update($update);*/
?>	