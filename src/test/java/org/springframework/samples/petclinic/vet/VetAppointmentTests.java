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

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.owner.Appointment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Vet} appointment load tracking.
 */
class VetAppointmentTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 6, 10);

	private static final LocalDate TOMORROW = LocalDate.of(2026, 6, 11);

	@Test
	void shouldTrackTotalAppointmentCount() {
		Vet vet = new Vet();
		vet.addAppointment(appointment(TODAY, "Checkup"));
		vet.addAppointment(appointment(TOMORROW, "Follow-up"));

		assertThat(vet.getNrOfAppointments()).isEqualTo(2);
	}

	@Test
	void shouldTrackDailyAppointmentLoad() {
		Vet vet = new Vet();
		vet.addAppointment(appointment(TODAY, "Morning visit"));
		vet.addAppointment(appointment(TODAY, "Afternoon visit"));
		vet.addAppointment(appointment(TOMORROW, "Next day visit"));

		assertThat(vet.getDailyAppointmentCount(TODAY)).isEqualTo(2);
		assertThat(vet.getDailyAppointmentCount(TOMORROW)).isEqualTo(1);
	}

	@Test
	void shouldBeAvailableWhenBelowDailyLimit() {
		Vet vet = new Vet();
		for (int i = 0; i < Vet.DAILY_APPOINTMENT_LIMIT - 1; i++) {
			vet.addAppointment(appointment(TODAY, "Visit " + i));
		}

		assertThat(vet.isAvailable(TODAY)).isTrue();
	}

	@Test
	void shouldBeUnavailableWhenDailyLimitReached() {
		Vet vet = new Vet();
		for (int i = 0; i < Vet.DAILY_APPOINTMENT_LIMIT; i++) {
			vet.addAppointment(appointment(TODAY, "Visit " + i));
		}

		assertThat(vet.isAvailable(TODAY)).isFalse();
	}

	@Test
	void shouldRemainAvailableOnOtherDaysWhenOneDayIsFullyBooked() {
		Vet vet = new Vet();
		for (int i = 0; i < Vet.DAILY_APPOINTMENT_LIMIT; i++) {
			vet.addAppointment(appointment(TODAY, "Visit " + i));
		}

		assertThat(vet.isAvailable(TODAY)).isFalse();
		assertThat(vet.isAvailable(TOMORROW)).isTrue();
	}

	@Test
	void shouldLinkAppointmentToVetWhenAdded() {
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");

		Appointment appointment = appointment(TODAY, "Dental cleaning");
		vet.addAppointment(appointment);

		assertThat(appointment.getVet()).isEqualTo(vet);
	}

	private Appointment appointment(LocalDate date, String description) {
		Appointment appointment = new Appointment();
		appointment.setDate(date);
		appointment.setDescription(description);
		return appointment;
	}

}
