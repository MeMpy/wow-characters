package test

import java.io.InputStream
import java.nio.file.{Files, Paths}

/**
  * Created by Ross on 8/14/2016.
  */
object UtilTest{

  //TODO use Path api to join path
  val testDataDefaultDir = "/testdata/"

  def readResource(resourceName:String):Array[Byte]={
    val resourcePath = Paths.get(getClass.getResource(testDataDefaultDir + resourceName).toURI)
    Files.readAllBytes(resourcePath)
  }

}
