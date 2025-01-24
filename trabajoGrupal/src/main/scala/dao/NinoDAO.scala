package dao

import doobie._
import doobie.implicits._
import cats.effect.IO
import cats.implicits._

import models.Nino
import config.Database

object NinoDAO {
  // Inserta un solo registro en la tabla "ninos"
  def insert(nino: Nino): ConnectionIO[Int] = {
    sql"""
     INSERT INTO ninos (nombre, edad, calificacion, genero)
     VALUES (
       ${nino.nombre},
       ${nino.edad},
       ${nino.calificacion},
       ${nino.genero}
     )
   """.update.run
  }

  // Inserta varios registros en la tabla "ninos"
  def insertAll(ninos: List[Nino]): IO[List[Int]] = {
    Database.transactor.use { xa =>
      ninos.traverse(n => insert(n).transact(xa))
    }
  }

  def select(): ConnectionIO[List[Nino]] = {
    sql"""
    SELECT nombre, edad, calificacion, genero
    FROM ninos
  """.query[Nino].to[List]
  }
}
