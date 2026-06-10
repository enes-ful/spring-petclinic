/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.owner.Appointment;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class VetAvailabilityCacheTests {

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private VetRepository vets;

	@Autowired
	private VetCaches vetCaches;

	@Autowired
	private EntityManager entityManager;

	@Test
	void shouldReflectDailyAppointmentLoadAfterCacheEviction() {
		LocalDate today = LocalDate.now();
		this.vets.findAll();

		Owner owner = this.owners.findById(1).orElseThrow();
		Pet pet = owner.getPet(1);
		Vet james = this.vets.findById(1).orElseThrow();
		for (int i = 0; i < Vet.DAILY_APPOINTMENT_LIMIT; i++) {
			Appointment appointment = new Appointment();
			appointment.setDate(today);
			appointment.setDescription("Visit " + i);
			appointment.setVet(james);
			pet.addAppointment(appointment);
		}
		this.owners.saveAndFlush(owner);
		this.vetCaches.evictAll();
		this.entityManager.clear();

		Vet reloaded = this.vets.findById(1).orElseThrow();
		assertThat(reloaded.getDailyAppointmentCount(today)).isEqualTo(Vet.DAILY_APPOINTMENT_LIMIT);
		assertThat(reloaded.isAvailable(today)).isFalse();

		Vet fromCachedQuery = this.vets.findAll()
			.stream()
			.filter(vet -> vet.getId().equals(1))
			.findFirst()
			.orElseThrow();
		assertThat(fromCachedQuery.isAvailable(today)).isFalse();
	}

}
