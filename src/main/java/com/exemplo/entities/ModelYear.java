package com.exemplo.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_year", uniqueConstraints = {
		@UniqueConstraint(name = "uk_model_year_code", columnNames = {"model_id", "year_code"})
})
public class ModelYear extends PanacheEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "model_id", nullable = false)
	public Model model;

	@Column(name = "year_model", nullable = false)
	public Integer yearModel;

	@Column(name = "fuel_code", nullable = false, length = 4)
	public String fuelCode;

	@Column(name = "fuel_name", nullable = false, length = 50)
	public String fuelName;

	@Column(name = "year_code", nullable = false, length = 16)
	public String yearCode;

	@Column(name = "created_at", nullable = false)
	public LocalDateTime createdAt;

	@Column(name = "updated_at")
	public LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	public LocalDateTime deletedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
