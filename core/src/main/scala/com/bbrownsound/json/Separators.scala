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

trait Separators {
  type Separator = Char

  val PeriodSeparator: Separator = '.'
  val CommaSeparator: Separator = ','
  val SemiColonSeparator: Separator = ';'
  val TabSeparator: Separator = '\t'
  val PipeSeparator: Separator = '|'
  val UnderscoreSeparator: Separator = '_'
  val DashSeparator: Separator = '-'

  implicit def stringToSeparator(str: String): Separator = str match {
    case c if c.headOption.contains(PeriodSeparator) => PeriodSeparator
    case c if c.headOption.contains(CommaSeparator) => CommaSeparator
    case c if c.headOption.contains(SemiColonSeparator) => SemiColonSeparator
    case c if c.headOption.contains(TabSeparator) => TabSeparator
    case c if c.headOption.contains(PipeSeparator) => PipeSeparator
    case c if c.headOption.contains(UnderscoreSeparator) => UnderscoreSeparator
    case c if c.headOption.contains(DashSeparator) => DashSeparator
    case c if c.headOption.isDefined => c.head
    case _ => PeriodSeparator
  }
}