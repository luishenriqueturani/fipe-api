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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price", uniqueConstraints = {
		@UniqueConstraint(name = "uk_price_model_year_month", columnNames = {"model_year_id", "reference_month"})
})
public class Price extends PanacheEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "model_year_id", nullable = false)
	public ModelYear modelYear;

	@Column(name = "reference_month", nullable = false, length = 7)
	public String referenceMonth; // YYYY-MM

	@Column(name = "value", nullable = false, precision = 12, scale = 2)
	public BigDecimal value;

	@Column(name = "currency", nullable = false, length = 3)
	public String currency = "BRL";

	@Column(name = "consulted_at", nullable = false)
	public LocalDateTime consultedAt;

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
