package com.example.findway.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.findway.domain.Trail
import com.example.findway.domain.TrailPoint

@Entity(tableName = "trails")
data class TrailEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val startedAtMillis: Long,
  val endedAtMillis: Long? = null,
)

@Entity(
  tableName = "breadcrumbs",
  foreignKeys = [
    ForeignKey(
      entity = TrailEntity::class,
      parentColumns = ["id"],
      childColumns = ["trailId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("trailId"), Index(value = ["trailId", "sequence"], unique = true)],
)
data class BreadcrumbEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val trailId: Long,
  val sequence: Int,
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float,
  val timestampMillis: Long,
)

data class TrailWithBreadcrumbs(
  @Embedded val trail: TrailEntity,
  @Relation(parentColumn = "id", entityColumn = "trailId") val breadcrumbs: List<BreadcrumbEntity>,
)

fun TrailWithBreadcrumbs.toDomain(): Trail =
  Trail(
    id = trail.id,
    name = trail.name,
    startedAtMillis = trail.startedAtMillis,
    endedAtMillis = trail.endedAtMillis,
    breadcrumbs =
      breadcrumbs.sortedBy(BreadcrumbEntity::sequence).map { breadcrumb ->
        TrailPoint(
          latitude = breadcrumb.latitude,
          longitude = breadcrumb.longitude,
          accuracyMeters = breadcrumb.accuracyMeters,
          timestampMillis = breadcrumb.timestampMillis,
        )
      },
  )
