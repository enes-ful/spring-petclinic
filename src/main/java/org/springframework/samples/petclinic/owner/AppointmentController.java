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
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetCaches;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.vet.VetSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class AppointmentController {

	private static final String VIEWS_APPOINTMENT_FORM = "pets/createOrUpdateAppointmentForm";

	private final OwnerRepository owners;

	private final VetRepository vets;

	private final VetCaches vetCaches;

	public AppointmentController(OwnerRepository owners, VetRepository vets, VetCaches vetCaches) {
		this.owners = owners;
		this.vets = vets;
		this.vetCaches = vetCaches;
	}

	@InitBinder("appointment")
	public void initAppointmentBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id", "vet", "pet");
	}

	@ModelAttribute("appointment")
	public Appointment loadPetWithAppointment(@PathVariable int ownerId, @PathVariable int petId,
			Map<String, Object> model) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		model.put("pet", pet);
		model.put("owner", owner);

		Appointment appointment = new Appointment();
		pet.addAppointment(appointment);
		return appointment;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String initAppointmentForm(Appointment appointment, ModelMap model) {
		model.addAttribute("vets", findAvailableVets(appointment.getDate()));
		return VIEWS_APPOINTMENT_FORM;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/available-vets")
	public @ResponseBody List<VetSummary> availableVets(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return findAvailableVets(date).stream().map(VetSummary::from).toList();
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String processAppointmentForm(@ModelAttribute Owner owner, @PathVariable int petId,
			@Valid Appointment appointment, BindingResult result,
			@RequestParam(value = "vetId", required = false) Integer vetId, ModelMap model,
			RedirectAttributes redirectAttributes) {

		LocalDate today = LocalDate.now();
		LocalDate appointmentDate = appointment.getDate();

		if (appointmentDate != null && appointmentDate.isBefore(today)) {
			result.rejectValue("date", "typeMismatch.date");
		}

		Vet vet = resolveVet(vetId, appointmentDate, result);

		if (result.hasErrors()) {
			model.addAttribute("vets", findAvailableVets(appointmentDate != null ? appointmentDate : today));
			return VIEWS_APPOINTMENT_FORM;
		}

		appointment.setVet(vet);
		owner.addAppointment(petId, appointment);
		this.owners.save(owner);
		this.vetCaches.evictAll();

		redirectAttributes.addFlashAttribute("message", "Your appointment has been booked");
		return "redirect:/owners/{ownerId}";
	}

	private List<Vet> findAvailableVets(LocalDate date) {
		return this.vets.findAll()
			.stream()
			.filter(vet -> vet.isAvailable(date))
			.sorted((left, right) -> left.getLastName().compareToIgnoreCase(right.getLastName()))
			.toList();
	}

	private Vet resolveVet(Integer vetId, LocalDate appointmentDate, BindingResult result) {
		if (vetId == null) {
			result.reject("vet.required");
			return null;
		}

		Optional<Vet> optionalVet = this.vets.findById(vetId);
		if (optionalVet.isEmpty()) {
			result.reject("notFound");
			return null;
		}

		Vet vet = optionalVet.get();
		if (appointmentDate != null && !vet.isAvailable(appointmentDate)) {
			result.reject("vet.notAvailable");
			return null;
		}

		return vet;
	}

}
