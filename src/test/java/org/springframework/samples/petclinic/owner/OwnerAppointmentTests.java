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
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vet.Vet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Owner} appointment aggregation.
 */
class OwnerAppointmentTests {

	@Test
	void shouldAggregateUpcomingAppointmentsFromAllPets() {
		Owner owner = new Owner();

		Pet leo = pet("Leo");
		Appointment leoAppointment = appointment(LocalDate.now().plusDays(5), "Checkup");
		leo.addAppointment(leoAppointment);

		Pet rosy = pet("Rosy");
		Appointment rosyAppointment = appointment(LocalDate.now().plusDays(10), "Vaccination");
		rosy.addAppointment(rosyAppointment);

		owner.getPets().add(leo);
		owner.getPets().add(rosy);

		List<Appointment> upcoming = owner.getUpcomingAppointments();

		assertThat(upcoming).containsExactly(leoAppointment, rosyAppointment);
	}

	@Test
	void shouldExcludePastAppointments() {
		Owner owner = new Owner();

		Pet pet = pet("Max");
		pet.addAppointment(appointment(LocalDate.now().minusDays(1), "Past visit"));
		pet.addAppointment(appointment(LocalDate.now().plusDays(1), "Future visit"));
		owner.getPets().add(pet);

		assertThat(owner.getUpcomingAppointments()).hasSize(1);
		assertThat(owner.getUpcomingAppointments().get(0).getDescription()).isEqualTo("Future visit");
	}

	@Test
	void shouldOrderAppointmentsByDate() {
		Owner owner = new Owner();

		Pet pet = pet("Basil");
		Appointment later = appointment(LocalDate.now().plusDays(20), "Later");
		Appointment sooner = appointment(LocalDate.now().plusDays(2), "Sooner");
		pet.addAppointment(later);
		pet.addAppointment(sooner);
		owner.getPets().add(pet);

		assertThat(owner.getUpcomingAppointments()).containsExactly(sooner, later);
	}

	@Test
	void shouldFindPetForAppointment() {
		Owner owner = new Owner();

		Pet leo = pet("Leo");
		Appointment appointment = appointment(LocalDate.now().plusDays(3), "Dental");
		leo.addAppointment(appointment);
		owner.getPets().add(leo);

		assertThat(owner.getPet(appointment)).isEqualTo(leo);
	}

	private Pet pet(String name) {
		Pet pet = new Pet();
		pet.setName(name);
		return pet;
	}

	private Appointment appointment(LocalDate date, String description) {
		Appointment appointment = new Appointment();
		appointment.setDate(date);
		appointment.setDescription(description);
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		appointment.setVet(vet);
		return appointment;
	}

}
