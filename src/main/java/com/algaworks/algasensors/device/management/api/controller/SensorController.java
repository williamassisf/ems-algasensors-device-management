package com.algaworks.algasensors.device.management.api.controller;

import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGenerator;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

	private final SensorRepository repository;

	@GetMapping
	public Page<SensorOutput> search(@PageableDefault Pageable pageable) {
		var sensors = repository.findAll(pageable);
		return sensors.map(this::convertToModel);
	}

	@GetMapping("/{sensorId}")
	public SensorOutput getSensor(@PathVariable TSID sensorId) {
		var sensor = repository.findById(new SensorId(sensorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return convertToModel(sensor);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SensorOutput createSensor(@RequestBody SensorInput input) {
		var sensor = Sensor.builder()
						   .id(new SensorId(IdGenerator.generateTSID()))
						   .name(input.getName())
						   .ip(input.getIp())
						   .location(input.getLocation())
						   .protocol(input.getProtocol())
						   .model(input.getModel())
						   .enabled(false)
						   .build();

		sensor = repository.save(sensor);
		return convertToModel(sensor);
	}

	@PutMapping("/{sensorId}")
	@ResponseStatus(HttpStatus.OK)
	public SensorOutput updateSensor(@RequestBody SensorInput input, @PathVariable TSID sensorId) {
		if (sensorId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		var sensor = repository.findById(new SensorId(sensorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		sensor.setName(input.getName());
		sensor.setIp(input.getIp());
		sensor.setLocation(input.getLocation());
		sensor.setProtocol(input.getProtocol());
		sensor.setModel(input.getModel());

		repository.save(sensor);
		return convertToModel(sensor);
	}

	@PutMapping("/{sensorId}/enable")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SensorOutput enableSensor(@PathVariable TSID sensorId) {
		if (sensorId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		var sensor = repository.findById(new SensorId(sensorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		sensor.setEnabled(true);

		repository.save(sensor);
		return convertToModel(sensor);
	}

	@DeleteMapping("/{sensorId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSensor(@PathVariable TSID sensorId) {
		if (sensorId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		var sensor = repository.findById(new SensorId(sensorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		repository.delete(sensor);
	}

	@DeleteMapping("/{sensorId}/enable")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SensorOutput disableSensor(@PathVariable TSID sensorId) {
		if (sensorId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		var sensor = repository.findById(new SensorId(sensorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		sensor.setEnabled(false);

		repository.save(sensor);
		return convertToModel(sensor);
	}

	private SensorOutput convertToModel(Sensor sensor) {
		return SensorOutput.builder()
						   .id(sensor.getId().getValue())
						   .name(sensor.getName())
						   .ip(sensor.getIp())
						   .location(sensor.getLocation())
						   .protocol(sensor.getProtocol())
						   .model(sensor.getModel())
						   .enabled(sensor.getEnabled())
						   .build();
	}
}
