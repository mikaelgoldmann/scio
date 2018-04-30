/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.nio

import com.spotify.scio.ScioContext
import com.spotify.scio.io.Tap
import com.spotify.scio.values.SCollection

import scala.concurrent.Future

/**
 * Base trait for all Read Write IO classes, every IO connector must implement this.
 * This trait has two abstract implicit methods #read, #write that need be implemented
 * in every subtype. Look at the [[com.spotify.scio.nio.TextIO]] subclass as reference
 * implementation.
 */
trait ScioIO[T] {

  // abstract types for read/write params.
  type ReadP
  type WriteP

  def read(sc: ScioContext, params: ReadP): SCollection[T]

  def write(data: SCollection[T], params: WriteP): Future[Tap[T]]

  def tap(read: ReadP): Tap[T]

  def id: String
}

object ScioIO {
  // scalastyle:off structural.type
  type ReadOnly[T, R] =
    ScioIO[T] {
      type ReadP = R
      type WriteP = Nothing
    }

  type Aux[T, R, W] =
    ScioIO[T] {
      type ReadP = R
      type WriteP = W
    }

  def ro[T](io: ScioIO[T]): ScioIO.ReadOnly[T, io.ReadP] =
    new ScioIO[T] {
      type ReadP = io.ReadP
      type WriteP = Nothing

      def read(sc: ScioContext, params: ReadP): SCollection[T] =
        io.read(sc, params)

      def write(data: SCollection[T], params: WriteP): Future[Tap[T]] =
        throw new IllegalStateException("read-only IO. This code should be unreachable")

      def tap(read: ReadP): Tap[T] =
        io.tap(read)

      def id: String = io.id
    }
  // scalastyle:on structural.type
}