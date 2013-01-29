package extensions

/*
 * Copyright 2011 Delving B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.bson.types.ObjectId
import play.api.mvc.Results.Status
import play.api.mvc.{JavascriptLitteral, PathBindable}
import play.api.data.format.Formatter
import play.api.data.FormError
import play.api.http.ContentTypes
import play.api.{Play, PlayException}
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import reflect.Manifest

/**
 * Framework extensions
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

trait Extensions {

  def Json(data: AnyRef, status: Int = 200) = Status(status)(JJson.generate(data)).as(ContentTypes.JSON)

  implicit def objectIdFormat: Formatter[ObjectId] = new Formatter[ObjectId] {
    def bind(key: String, data: Map[String, String]) = data.get(key) match {
      case Some(oid) if ObjectId.isValid(oid) => Some(new ObjectId(oid)).toRight(Seq(FormError(key, "error.objectId", Nil)))
      case _ => Left(Seq(FormError(key, "error.objectId", Nil)))
    }

    def unbind(key: String, value: ObjectId) = Map(key -> value.toString)
  }


}

object Formatters {

  implicit def objectIdFormat: Formatter[ObjectId] = new Formatter[ObjectId] {
    def bind(key: String, data: Map[String, String]) = data.get(key) match {
      case Some(oid) if ObjectId.isValid(oid) => Some(new ObjectId(oid)).toRight(Seq(FormError(key, "error.objectId", Nil)))
      case _ => Left(Seq(FormError(key, "error.objectId", Nil)))
    }

    def unbind(key: String, value: ObjectId) = Map(key -> value.toString)
  }

  /** url-encoded simple map **/
  implicit def mapFormat: Formatter[Map[String, String]] = new Formatter[Map[String, String]] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Map[String, String]] = {

      val MapKey = """%s\[([^\]]*)\]""".format(key).r

      val bound: Map[String, String] = data.filter(t => MapKey.findFirstIn(t._1).isDefined).map(_._1).collect {
        case MapKey(mapKey) => (mapKey, data.get(key + "[" + mapKey + "]").getOrElse(Left(Seq(FormError(key, "Cannot retrieve value with key %s, map values %s".format(key + "[" + mapKey + "]", data.toString()), Nil)))).toString)
      }.toMap[String, String]

      Right(bound)
    }

    def unbind(key: String, value: Map[String, String]) = value.map(t => (key + "[" + t._1 + "]" -> t._2))
  }

}

object Binders {

  implicit def bindableOption[T: PathBindable] = new PathBindable[Option[T]] {
    def bind(key: String, value: String) = {
      implicitly[PathBindable[T]].bind(key, value).right.map(Some(_))
    }
    def unbind(key: String, value: Option[T]) = value.map(v => implicitly[PathBindable[T]].unbind(key, v)).getOrElse("")
  }

  implicit def bindableObjectId = new PathBindable[ObjectId] {
    def bind(key: String, value: String) = {
      if (ObjectId.isValid(value)) {
        Right(new ObjectId(value))
      } else {
        Left("Cannot parse parameter " + key + " as BSON ObjectId")
      }
    }

    def unbind(key: String, value: ObjectId) = value.toString
  }

  implicit def bindableJavascriptLitteral = new JavascriptLitteral[ObjectId] {
    def to(value: ObjectId) = value.toString
  }

}

object JJson {

  implicit val formats = DefaultFormats + new ObjectIdSerializer

  def generate[A <: AnyRef](a: A) = write(a)

  def parse[A](json: String)(implicit mf: Manifest[A]) = read[A](json)

}

class ObjectIdSerializer extends Serializer[ObjectId] {
    private val Class = classOf[ObjectId]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Class, _), json) => json match {
        case JObject(JField("id", JString(s)) :: Nil) if ObjectId.isValid(s) => new ObjectId(s)
        case x => throw new MappingException("Can't convert " + x + " to ObjectId")
      }
    }

    def serialize(implicit format: Formats) = {
      case x: ObjectId => JString(x.toString)
    }
  }



// ~~~ Exceptions

object ProgrammerException {
  def apply(message: String) = new PlayException("Programmer Exception", message)
}

object ConfigurationException {
  def apply(message: String) = new PlayException("Configuration Exception", message)
}
