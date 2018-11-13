/*
 *
 * Copyright 2018 Brandon Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.bbrownsound.json.flattener

import java.io.{InputStreamReader, StringWriter}
import better.files.File
import com.bbrownsound.json._
import com.eclipsesource.json.{Json, WriterConfig}
import com.github.wnameless.json.flattener.{JsonFlattener, PrintMode}
import com.github.wnameless.json.unflattener.JsonUnflattener
import org.scalatest.{Assertion, FlatSpec, Matchers}

trait TestHelpers extends FlatSpec with Matchers {
  def loadResource(fileName: String) = {
    File(s"./core/src/test/resources/${fileName}.json").contentAsString
  }

  def loadResourceAsStream(fileName: String): InputStreamReader = {
    new InputStreamReader(File(s"./core/src/test/resources/${fileName}.json").newInputStream)
  }

  def runPrintFlattenTest(src: String, pm: PrintMode, wc: WriterConfig): Assertion = {
    val json = new JsonFlattener(src).keepArrays.print(pm).flatten
    val sw = new StringWriter()
    Json.parse(json).writeTo(sw, wc)
    json shouldBe sw.toString
  }

  def runPrintUnFlattenTest(src: String, pm: PrintMode, wc: WriterConfig): Assertion = {
    val json = new JsonUnflattener(src).print(pm).unflatten
    val sw = new StringWriter()
    Json.parse(json).writeTo(sw, wc)
    json shouldBe sw.toString
  }

  val expectedFlattened1: String = "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"
  val testString: String = loadResource("test")
  val testString2: String = loadResource("test2")
  val testString3: String = loadResource("test3")
  val testString4: String = loadResource("test4")
  val testString5: String = loadResource("test5")
  val testString6: String = loadResource("test6")
  val testStringMongo: String = loadResource("test_mongo")
  val testStringMongoFlattened: String = loadResource("test_mongo_flattened")
}
