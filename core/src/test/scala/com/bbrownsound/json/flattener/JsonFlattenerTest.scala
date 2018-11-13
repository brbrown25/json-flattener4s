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

import java.util.Collections
import com.bbrownsound.json._
import com.eclipsesource.json.{PrettyPrint, WriterConfig}
import com.github.wnameless.json.flattener.{JsonFlattener, KeyTransformer, PrintMode, StringEscapePolicy}
import com.github.wnameless.json.unflattener.JsonUnflattener
import org.scalatest._
import scala.collection.JavaConverters._
import scala.collection.mutable

class JsonFlattenerTest extends TestHelpers {
  it should "testFlattenAsMap" in {
    val json = testString2
    JsonFlattener.flattenAsMap(json).toString shouldBe expectedFlattened1
  }

  it should "testFlatten" in {
    val json = testString2
    JsonFlattener.flatten(json) shouldBe expectedFlattened1
    val expected = "{\"[0].a\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}"
    JsonFlattener.flatten("[{\"a\":1},2,{\"c\":[3,4]}]") shouldBe expected
  }

  it should "testFlattenWithKeyContainsDotAndSquareBracket" in {
    val expected = "{\"[0][\\\"a.a.[\\\"]\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}"
    JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]") shouldBe expected
  }

  it should "testHashCode" in {
    val json1 = testString
    val json2 = testString2
    val flattener = new JsonFlattener(json1)
    flattener.hashCode shouldBe flattener.hashCode
    new JsonFlattener(json1).hashCode shouldBe flattener.hashCode
    new JsonFlattener(json2).hashCode shouldNot be(flattener.hashCode)
  }

  it should "testEquals" in {
    val flattener = new JsonFlattener(testString)
    flattener.equals(flattener) shouldBe true
    flattener.equals(new JsonFlattener(testString)) shouldBe true
    flattener.equals(new JsonFlattener(testString2)) shouldBe false
    flattener.equals(123L) shouldBe false
  }

  it should "testToString" in {
    val json = testString2
    val expected = "JsonFlattener{source={\"a\":{\"b\":1,\"c\":null,\"d\":[false,true]},\"e\":\"f\",\"g\":2.3}}"
    new JsonFlattener(json).toString shouldBe expected
  }

  it should "testWithNoPrecisionDouble" in {
    val json = "{\"39473331\":{\"mega\":6.0,\"goals\":1.0}}"
    new JsonFlattener(json).flatten shouldBe "{\"39473331.mega\":6.0,\"39473331.goals\":1.0}"
  }

  it should "testWithEmptyJsonObject" in {
    val json = "{}"
    new JsonFlattener(json).flatten shouldBe json
    JsonUnflattener.unflatten(new JsonFlattener(json).flatten) shouldBe json
    new JsonFlattener(json).toScalaMap shouldBe mutable.HashMap()
  }

  it should "testWithEmptyJsonArray() throws IOException" in {
    val json = "[]"
    json shouldBe new JsonFlattener(json).flatten
    Map("root" -> seqAsJavaList(Seq())) shouldBe new JsonFlattener(json).flattenAsMap.asScala
    json shouldBe JsonUnflattener.unflatten(new JsonFlattener(json).flatten())
    json shouldBe new JsonFlattener(json).keepArrays.flatten
    Map("root" -> seqAsJavaList(Seq())) shouldBe new JsonFlattener(json).keepArrays.flattenAsMap().asScala
    json shouldBe JsonUnflattener.unflatten(new JsonFlattener(json).keepArrays.flatten)
  }

  it should "testWithEmptyArray" in {
    val json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}"
    "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}" shouldBe new JsonFlattener(json).flatten
    json shouldBe JsonUnflattener.unflatten(new JsonFlattener(json).flatten)
  }

  it should "testWithEmptyObject" in {
    val json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[{}]}"
    "{\"no\":\"1\",\"name\":\"riya\",\"marks[0]\":{}}" shouldBe new JsonFlattener(json).flatten
    json shouldBe JsonUnflattener.unflatten(new JsonFlattener(json).flatten)
  }

  it should "testWithArray" in {
    val json = "[{\"abc\":123},456,[null]]"
    "{\"[0].abc\":123,\"[1]\":456,\"[2][0]\":null}" shouldBe new JsonFlattener(json).flatten
    json shouldBe JsonUnflattener.unflatten(new JsonFlattener(json).flatten)
  }

  it should "testWithSpecialCharacters" in {
    val json = "[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"
    "{\"[0].abc\\t\":\" \\\" \\r \\t \1234 \"}" shouldBe new JsonFlattener(json).flatten
    val json2 = "{\" \":[123,\"abc\"]}"
    "{\" [0]\":123,\" [1]\":\"abc\"}" shouldBe new JsonFlattener(json2).flatten
  }

  it should "testWithUnicodeCharacters" in {
    val json = "[{\"姓名\":123}]"
    "{\"[0].姓名\":123}" shouldBe new JsonFlattener(json).flatten
  }

  it should "testWithFlattenMode() throws IOException" in {
    val expected = "{\"a.b\":1,\"a.c\":null,\"a.d\":[false,{\"i.j\":[false,true,\"xy\"]}],\"e\":\"f\",\"g\":2.3,\"z\":[]}"
    expected shouldBe new JsonFlattener(testString4).keepArrays.flatten
  }

  it should "testWithStringEscapePolicyALL_UNICODES" in {
    val json = "{\"abc\":{\"def\":\"太極\"}}"
    val expected = "{\"abc.def\":\"\\u592A\\u6975\"}"
    expected shouldBe new JsonFlattener(json).escapePolicy(StringEscapePolicy.ALL_UNICODES).flatten
  }

  it should "testWithStringEscapePolicyALL" in {
    val json = "{\"abc\":{\"def\":\"太極/兩儀\"}}"
    val expected = "{\"abc.def\":\"\\u592A\\u6975\\/\\u5169\\u5100\"}"
    expected shouldBe new JsonFlattener(json).escapePolicy(StringEscapePolicy.ALL).flatten
  }

  it should "testWithStringEscapePolicyALL_BUT_SLASH" in {
    val json = "{\"abc\":{\"def\":\"太極/兩儀\"}}"
    val expected = "{\"abc.def\":\"\\u592A\\u6975/\\u5169\\u5100\"}"
    expected shouldBe new JsonFlattener(json).escapePolicy(StringEscapePolicy.ALL_BUT_SLASH).flatten
  }

  it should "testWithStringEscapePolicyALL_BUT_UNICODE" in {
    val json = "{\"abc\":{\"def\":\"太極/兩儀\"}}"
    val expected = "{\"abc.def\":\"太極\\/兩儀\"}"
    expected shouldBe new JsonFlattener(json).escapePolicy(StringEscapePolicy.ALL_BUT_UNICODE).flatten
  }

  it should "testWithStringEscapePolicyALL_BUT_SLASH_AND_UNICODE" in {
    val json = "{\"abc\":{\"def\":\"太極/兩儀\"}}"
    val expected = "{\"abc.def\":\"太極/兩儀\"}"
    expected shouldBe new JsonFlattener(json).escapePolicy(StringEscapePolicy.ALL_BUT_SLASH_AND_UNICODE).flatten
  }

  it should "testWithSeparator" in {
    val json = "{\"abc\":{\"def\":123}}"
    val expected = "{\"abc*def\":123}"
    expected shouldBe new JsonFlattener(json).separator('*').flatten
  }

  it should "testWithSeparatorExceptions" in {
    def shouldThrow(separator: Char, msg: String): Assertion = {
      val json = "{\"abc\":{\"def\":123}}"
      val expected = intercept[IllegalArgumentException](new JsonFlattener(json).separator(separator)).getMessage
      msg shouldBe expected
    }

    shouldThrow('"', "Separator contains illegal chracter(\")")
    shouldThrow(' ', "Separator contains illegal chracter( )")
    shouldThrow('[', "Separator([) is already used in brackets")
    shouldThrow(']', "Separator(]) is already used in brackets")
  }

  it should "testWithLeftAndRightBracket" in {
    val json = "{\"abc\":{\"A.\":[123,\"def\"]}}"
    val expected = "{\"abc{\\\"A.\\\"}{0}\":123,\"abc{\\\"A.\\\"}{1}\":\"def\"}"
    new JsonFlattener(json).brackets('{', '}').flatten() shouldBe expected
  }

  it should "testWithLeftAndRightBracketsExceptions" in {
    def shouldThrow(brackets: (Char, Char), msg: String): Assertion = {
      val json = "{\"abc\":{\"def\":123}}"
      val (l, r) = brackets
      val expected = intercept[IllegalArgumentException](new JsonFlattener(json).brackets(l, r)).getMessage
      msg shouldBe expected
    }

    shouldThrow(('#', '#'), "Both brackets cannot be the same")
    shouldThrow(('"', ']'), "Left bracket contains illegal chracter(\")")
    shouldThrow((' ', ']'), "Left bracket contains illegal chracter( )")
    shouldThrow(('.', ']'), "Left bracket contains illegal chracter(.)")
    shouldThrow(('[', '"'), "Right bracket contains illegal chracter(\")")
    shouldThrow(('[', ' '), "Right bracket contains illegal chracter( )")
    shouldThrow(('[', '.'), "Right bracket contains illegal chracter(.)")
  }


  it should "test root in map" in {
    JsonFlattener.flatten("null") shouldBe "null"
    JsonFlattener.flattenAsMap("null").get("root") shouldBe null
    JsonFlattener.flatten("123") shouldBe "123"
    JsonFlattener.flattenAsMap("123").get("root") shouldBe BigDecimal("123").bigDecimal
    JsonFlattener.flatten("\"abc\"") shouldBe "\"abc\""
    JsonFlattener.flattenAsMap("\"abc\"").get("root") shouldBe "abc"
    JsonFlattener.flatten("true") shouldBe "true"
    JsonFlattener.flattenAsMap("true").get("root") shouldBe true
    JsonFlattener.flatten("[]") shouldBe "[]"
    JsonFlattener.flattenAsMap("[]").get("root") shouldBe Collections.emptyList()
    new JsonFlattener("[[{\"abc\":{\"def\":123}}]]").keepArrays.flatten shouldBe "[[{\"abc.def\":123}]]"
    val root: java.util.List[java.util.List[java.util.Map[String, Object]]] = new JsonFlattener("[[{\"abc\":{\"def\":123}}]]").keepArrays.flattenAsMap.get("root").asInstanceOf[java.util.List[java.util.List[java.util.Map[String, Object]]]]
    mapAsScalaMap(root.get(0).get(0)) shouldBe Map("abc.def" -> BigDecimal(123).bigDecimal)
  }

  it should "test PrintMode" in {
    val src1 = testString
    runPrintFlattenTest(src1, PrintMode.MINIMAL, WriterConfig.MINIMAL)
    runPrintFlattenTest(src1, PrintMode.REGULAR, PrettyPrint.singleLine)
    runPrintFlattenTest(src1, PrintMode.PRETTY, WriterConfig.PRETTY_PRINT)

    val src2 = "[[123]]"
    runPrintFlattenTest(src2, PrintMode.MINIMAL, WriterConfig.MINIMAL)
    runPrintFlattenTest(src2, PrintMode.REGULAR, PrettyPrint.singleLine)
    runPrintFlattenTest(src2, PrintMode.PRETTY, WriterConfig.PRETTY_PRINT)
  }

  it should "test no cache" in {
    val jf = new JsonFlattener("{\"abc\":{\"def\":123}}")
    jf.flattenAsMap shouldBe jf.flattenAsMap
    jf.flatten shouldNot equal(jf.flattenAsMap)
    jf.separator('*').flatten shouldBe "{\"abc*def\":123}"
    jf.flatten shouldNot equal(jf.reg.flatten)
  }

  it should "test NPE" in {
    val json = "{\"abc\":{\"def\":123}}"

    intercept[NullPointerException](new JsonFlattener(json).mode(null))
    intercept[NullPointerException](new JsonFlattener(json).escapePolicy(null))
    intercept[NullPointerException](new JsonFlattener(json).print(null))
  }

  it should "test flattent with nested empty json and keep array mode" in {
    val expected = "{\"a.b\":1,\"a.c\":null,\"a.d\":[false,{\"i.j\":[false,true]}],\"e\":\"f\",\"g\":2.3,\"z\":{}}"
    val jf = new JsonFlattener(testString5).keepArrays

    jf.flatten shouldBe expected
  }

  it should "test separator and nested object" in {
    val expected = "{\"a_b\":1,\"a_c\":null,\"a_d\":[false,{\"i_j\":[false,true]}],\"e\":\"f\",\"g\":2.3,\"z\":{}}"
    val jf = new JsonFlattener(testString5).keepArrays.underscoreSep

    jf.flatten shouldBe expected
  }

  it should "test with Root Key in Source Object" in {
    val json = "{\"" + JsonFlattener.ROOT + "\":null, \"ss\":[123]}"
    val expected = "{\"" + JsonFlattener.ROOT + "\":null,\"ss[0]\":123}"
    JsonFlattener.flatten(json) shouldBe expected
  }

  it should "init with reader" in {
    val jsonStream = loadResourceAsStream("test")
    val jsonString = testString

    new JsonFlattener(jsonStream) shouldBe new JsonFlattener(jsonString)
  }

  it should "flatten in MongoDB Mode" in {
    val flattened = new JsonFlattener(testStringMongo).mongo.pretty.flatten

    flattened shouldBe testStringMongoFlattened
  }

  it should "throw mongo flatten exception" in {
    val json = "{\"abc\":{\"de.f\":123}}"
    val jf = new JsonFlattener(json).mongo

    intercept[IllegalArgumentException](jf.flatten).getMessage shouldBe
      "Key cannot contain separator(.) in FlattenMode.MONGODB"
  }

  it should "transform with Key Transformer" in {
    val json = "{\"abc\":{\"de.f\":123}}"
    val expected = "{\"abc.de_f\":123}"
    val transformer = new KeyTransformer {
      override def transform(key: String): String = key.replace('.', '_')
    }
    val jf = new JsonFlattener(json).mongo.keyTransform(transformer)

    jf.flatten shouldBe expected
  }
}