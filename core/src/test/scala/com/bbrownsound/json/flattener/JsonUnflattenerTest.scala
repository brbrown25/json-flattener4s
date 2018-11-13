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
import java.io.StringReader
import com.bbrownsound.json._
import com.bbrownsound.json.flattener.TestHelpers
import com.eclipsesource.json.{Json, PrettyPrint, WriterConfig}
import com.github.wnameless.json.flattener.{JsonFlattener, KeyTransformer, PrintMode}
import com.github.wnameless.json.unflattener.JsonUnflattener
import org.scalatest._

class JsonUnflattenerTest extends TestHelpers {
  it should "testUnflattenWithArrayOfNestedObjectsInValByKeepArraysMode()" in {
    val flattendJson = new JsonFlattener(testString6).keepArrays.flatten
    flattendJson shouldBe "{\"a\":[1,2,3],\"b\":[{\"c.d\":[1,2]}]}"
    JsonUnflattener.unflatten(flattendJson) shouldBe "{\"a\":[1,2,3],\"b\":[{\"c\":{\"d\":[1,2]}}]}"
  }

  it should "testUnflatten()" in {
    JsonUnflattener.unflatten(
      "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}") shouldBe "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}"
    JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}") shouldBe "[1,[2,3],4,{\"abc\":5}]"
    JsonUnflattener.unflatten(JsonFlattener.flatten("{\" \\\"abc\":{\"def \":123}}")) shouldBe "{\" \\\"abc\":{\"def \":123}}"
    JsonUnflattener.unflatten(JsonFlattener.flatten("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]")) shouldBe "[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"

  }

  it should "testUnflattenWithKeyContainsDotAndSquareBracket()" in {
    JsonUnflattener.unflatten("{\"[1][0]\":2,\"[ 0 ]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][ \\\"ab.c.[\\\" ]\":5}") shouldBe "[1,[2,3],4,{\"ab.c.[\":5}]"
  }

  it should "testUnflattenWithReversedIndexesWithinObjects()" in {
    JsonUnflattener.unflatten(testString3) shouldBe "{\"List\":[{\"type\":\"A\"},null,{\"type\":\"B\"}]}"
  }

  it should "testUnflattenWithReversedIndexes()" in {
    val json = "{\"[1][1]\":\"B\",\"[0][0]\":\"A\"}"
    JsonUnflattener.unflatten(json) shouldBe "[[\"A\"],[null,\"B\"]]"
  }

  it should "testUnflattenWithInitComplexKey()" in {
    val json = "{\"[\\\"b.b\\\"].aaa\":123}"
    JsonUnflattener.unflatten(json) shouldBe "{\"b.b\":{\"aaa\":123}}"
  }

  it should "testHashCode()" in {
    val json1 = "[[123]]"
    val unflattener = new JsonUnflattener(json1)
    unflattener.hashCode() shouldBe unflattener.hashCode()
    new JsonUnflattener(json1).hashCode() shouldBe unflattener.hashCode()
    new JsonUnflattener("[[[123]]]").hashCode() shouldNot equal(unflattener.hashCode())
  }

  it should "testEquals()" in {
    val json1 = "[[123]]"
    val unflattener = new JsonUnflattener(json1)
    unflattener.equals(unflattener) shouldBe true
    unflattener.equals(new JsonUnflattener(json1)) shouldBe true
    unflattener.equals(new JsonUnflattener("[[[123]]]")) shouldBe false
    unflattener.equals(123L) shouldBe false
  }

  it should "testToString()" in {
    new JsonUnflattener("[[123]]").toString() shouldBe "JsonUnflattener{root=[[123]]}"
  }

  it should "testWithKeepArrays()" in {
    JsonUnflattener.unflatten(new JsonFlattener(testString4).keepArrays.flatten) shouldBe testString4
  }

  it should "testWithSeparater()" in {
    val json = "{\"abc\":{\"def\":123}}"
    new JsonUnflattener(new JsonFlattener(json).separator('*').flatten).separator('*').unflatten shouldBe json
  }

  it should "testWithSeparaterExceptions()" in {
    def shouldThrow(separator: Char, msg: String): Assertion = {
      val json = "{\"abc\":{\"def\":123}}"
      val expected = intercept[IllegalArgumentException](new JsonUnflattener(json).separator(separator)).getMessage
      msg shouldBe expected
    }

    shouldThrow('"', "Separator contains illegal chracter(\")")
    shouldThrow(' ', "Separator contains illegal chracter( )")
    shouldThrow('[', "Separator([) is already used in brackets")
    shouldThrow(']', "Separator(]) is already used in brackets")
  }

  it should "testWithLeftAndRightBrackets()" in {
    new JsonUnflattener("{\"abc[\\\"A.\\\"][0]\":123}").brackets('[', ']').unflatten shouldBe "{\"abc\":{\"A.\":[123]}}"
    new JsonUnflattener("{\"abc{\\\"A.\\\"}{0}\":123}").brackets('{', '}').unflatten shouldBe "{\"abc\":{\"A.\":[123]}}"
  }

  it should "testWithLeftAndRightBracketsExceptions()" in {
    def shouldThrow(brackets: (Char, Char), msg: String): Assertion = {
      val json = "{\"abc\":{\"def\":123}}"
      val (l, r) = brackets
      val expected = intercept[IllegalArgumentException](new JsonUnflattener(json).brackets(l, r)).getMessage
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

  it should "testWithNonObject()" in {
    JsonUnflattener.unflatten("123") shouldBe "123"
    JsonUnflattener.unflatten("\"abc\"") shouldBe "\"abc\""
    JsonUnflattener.unflatten("true") shouldBe "true"
    JsonUnflattener.unflatten("[1,2,3]") shouldBe "[1,2,3]"
  }

  it should "testWithNestedArrays()" in {
    JsonUnflattener.unflatten("[[{\"abc.def\":123}]]") shouldBe "[[{\"abc\":{\"def\":123}}]]"
  }

  it should "testPrintMode()" in {
    val src = "{\"abc.def\":123}"
    runPrintUnFlattenTest(src, PrintMode.MINIMAL, WriterConfig.MINIMAL)
    runPrintUnFlattenTest(src, PrintMode.REGULAR, PrettyPrint.singleLine)
    runPrintUnFlattenTest(src, PrintMode.PRETTY, WriterConfig.PRETTY_PRINT)
  }

  it should "testNoCache()" in {
    val ju = new JsonUnflattener("{\"abc.def\":123}")
    ju.unflatten shouldBe ju.unflatten
    ju.reg.unflatten shouldBe "{\"abc\": {\"def\": 123}}"
  }

  it should "testNullPointerException()" in {
    intercept[NullPointerException](new JsonUnflattener("{\"abc.def\":123}").print(null))
  }

  it should "testInitByReader()" in {
    new JsonUnflattener("{\"abc.def\":123}") shouldBe new JsonUnflattener(new StringReader("{\"abc.def\":123}"))
  }

  it should "testFlattenModeMongodb()" in {
    val ju = new JsonUnflattener(testStringMongoFlattened).mongo
    Json.parse(testStringMongo).toString() shouldEqual ju.unflatten()
  }

  it should "testWithKeyTransformer()" in {
    val json = "{\"abc.de_f\":123}"
    val kt = new KeyTransformer() {
      override def transform(key: String): String = key.replace('_', '.')
    }
    val ju = new JsonUnflattener(json).mongo.keyTransform(kt)
    ju.unflatten shouldBe "{\"abc\":{\"de.f\":123}}"
  }
}
