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
package com.bbrownsound.json

import com.github.wnameless.json.flattener._
import com.github.wnameless.json.unflattener.JsonUnflattener
import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

//todo add some scalaz goodness
final class JsonFlattenerOps(self: JsonFlattener) extends Separators {
  def mode(mode: FlattenMode): JsonFlattener = self.withFlattenMode(mode)
  def normal: JsonFlattener = mode(FlattenMode.NORMAL)
  def keepArrays: JsonFlattener = mode(FlattenMode.KEEP_ARRAYS)
  def mongo: JsonFlattener = mode(FlattenMode.MONGODB)

  def print(mode: PrintMode): JsonFlattener = self.withPrintMode(mode)
  def min: JsonFlattener = print(PrintMode.MINIMAL)
  def reg: JsonFlattener = print(PrintMode.REGULAR)
  def pretty: JsonFlattener = print(PrintMode.PRETTY)

  def escapePolicy(policy: CharSequenceTranslatorFactory): JsonFlattener = self.withStringEscapePolicy(policy)

  def keyTransform(transformer: KeyTransformer): JsonFlattener = self.withKeyTransformer(transformer)

  def separator(sep: Separator): JsonFlattener = self.withSeparator(sep)
  def periodSep: JsonFlattener = separator(PeriodSeparator)
  def commaSep: JsonFlattener = separator(CommaSeparator)
  def semiColonSep: JsonFlattener = separator(SemiColonSeparator)
  def tabSep: JsonFlattener = separator(TabSeparator)
  def pipeSep: JsonFlattener = separator(PipeSeparator)
  def underscoreSep: JsonFlattener = separator(UnderscoreSeparator)
  def dashSep: JsonFlattener = separator(DashSeparator)

  def brackets(left: Char, right: Char): JsonFlattener = self.withLeftAndRightBrackets(left, right)

  def toScalaMap: MMap[String, AnyRef] = mapAsScalaMap(self.flattenAsMap)
}

final class JsonUnflattenerOps(self: JsonUnflattener) extends Separators {
  def mode(mode: FlattenMode): JsonUnflattener = self.withFlattenMode(mode)
  def normal: JsonUnflattener = mode(FlattenMode.NORMAL)
  def keepArrays: JsonUnflattener = mode(FlattenMode.KEEP_ARRAYS)
  def mongo: JsonUnflattener = mode(FlattenMode.MONGODB)

  def print(mode: PrintMode): JsonUnflattener = self.withPrintMode(mode)
  def min: JsonUnflattener = print(PrintMode.MINIMAL)
  def reg: JsonUnflattener = print(PrintMode.REGULAR)
  def pretty: JsonUnflattener = print(PrintMode.PRETTY)

  def keyTransform(transformer: KeyTransformer): JsonUnflattener = self.withKeyTransformer(transformer)

  def separator(sep: Separator): JsonUnflattener = self.withSeparator(sep)
  def periodSep: JsonUnflattener = separator(PeriodSeparator)
  def commaSep: JsonUnflattener = separator(CommaSeparator)
  def semiColonSep: JsonUnflattener = separator(SemiColonSeparator)
  def tabSep: JsonUnflattener = separator(TabSeparator)
  def pipeSep: JsonUnflattener = separator(PipeSeparator)
  def underscoreSep: JsonUnflattener = separator(UnderscoreSeparator)
  def dashSep: JsonUnflattener = separator(DashSeparator)

  def brackets(left: Char, right: Char): JsonUnflattener = self.withLeftAndRightBrackets(left, right)
}

trait ToFlattenerOps {
  implicit def JsonFlattenerOps(jf: JsonFlattener): JsonFlattenerOps = new JsonFlattenerOps(jf)
  implicit def JsonUnFlattenerOps(juf: JsonUnflattener): JsonUnflattenerOps = new JsonUnflattenerOps(juf)
}

