import cats.effect.{IO, IOApp}
import config.Database
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import doobie._
import doobie.implicits._
import cats.effect.IO
import cats.implicits.toTraverseOps
import doobie.ConnectionIO

import java.io.File
import models.Nino
import dao.NinoDAO

// Extiende de IOApp.Simple para manejar efectos IO y recursos de forma segura
object Main extends IOApp.Simple {
  val path2DataFile2 = "src/main/resources/data/niños.csv"

  val dataSource = new File(path2DataFile2)
    .readCsv[List, Nino](rfc.withHeader.withCellSeparator(','))

  val ninos = dataSource.collect {
    case Right(ninos) => ninos
  }

  /**
   * Punto de entrada principal de la aplicación.
   * Lee temperaturas desde CSV, las inserta en la base de datos,
   * e imprime el número de registros insertados.
   *
   * @return IO[Unit] que representa la secuencia de operaciones
   */
  def run: IO[Unit] =
    for {
      _ <- NinoDAO.insertAll(ninos)
        .flatMap(result => IO.println(s"Registros insertados: ${result.size}"))
      _ <- Database.transactor.use { xa =>
        NinoDAO.select()
          .transact(xa)
          .flatMap(ninos =>
            ninos.traverse(nino =>
              IO.println(s"Niño: ${nino.nombre}, Edad: ${nino.edad}, Calificación: ${nino.calificacion}, Género: ${nino.genero}")
            )
          )
      }
    } yield ()
}