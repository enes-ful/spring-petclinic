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

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.util.SerializationUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Pet} appointment management.
 */
class PetAppointmentTests {

	@Test
	void shouldAddAppointmentToPet() {
		Pet pet = new Pet();
		pet.setName("Leo");

		Appointment appointment = new Appointment();
		appointment.setDate(LocalDate.of(2026, 6, 15));
		appointment.setDescription("Annual checkup");

		pet.addAppointment(appointment);

		assertThat(pet.getAppointments()).hasSize(1);
		assertThat(pet.getAppointments()).contains(appointment);
	}

	@Test
	void shouldUpdateAppointmentHistoryWhenNewAppointmentIsAdded() {
		Pet pet = new Pet();
		pet.setName("Rosy");

		Appointment first = new Appointment();
		first.setDate(LocalDate.of(2026, 6, 1));
		first.setDescription("Checkup");

		Appointment second = new Appointment();
		second.setDate(LocalDate.of(2026, 7, 1));
		second.setDescription("Vaccination");

		pet.addAppointment(first);
		pet.addAppointment(second);

		assertThat(pet.getAppointments()).hasSize(2);
		assertThat(pet.getAppointments()).containsExactly(first, second);
	}

	@Test
	void shouldAssociateVetWithAppointment() {
		Pet pet = new Pet();
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");

		Appointment appointment = new Appointment();
		appointment.setDescription("Dental cleaning");
		appointment.setVet(vet);

		pet.addAppointment(appointment);

		assertThat(appointment.getVet()).isEqualTo(vet);
		assertThat(pet.getAppointments().iterator().next().getVet().getLastName()).isEqualTo("Carter");
	}

	@Test
	void serialization() {
		Pet pet = new Pet();
		pet.setName("Max");
		pet.setId(1);

		Appointment appointment = new Appointment();
		appointment.setDate(LocalDate.of(2026, 8, 1));
		appointment.setDescription("Follow-up");
		appointment.setId(10);
		pet.addAppointment(appointment);

		@SuppressWarnings("deprecation")
		Pet other = (Pet) SerializationUtils.deserialize(SerializationUtils.serialize(pet));
		assertThat(other.getName()).isEqualTo(pet.getName());
		assertThat(other.getId()).isEqualTo(pet.getId());
		assertThat(other.getAppointments()).hasSize(1);
	}

}
