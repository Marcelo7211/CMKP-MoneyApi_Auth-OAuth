package com.cmkpmoney.api.resourse;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cmkpmoney.api.event.RecursoCriadoEvent;
import com.cmkpmoney.api.model.Pessoa;
import com.cmkpmoney.api.repository.PessoaRepository;
import com.cmkpmoney.api.service.PessoaService;

@RestController
@RequestMapping("/pessoas")
public class PessoaResourse {

	@Autowired
	private PessoaRepository pessoaRepository;

	// Injetando o publisher (responsabel por chamar os eventos)
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired
	private PessoaService pessoaService;

	@GetMapping
	public ResponseEntity<List<Pessoa>> listarPessoas() {
		return ResponseEntity.ok(pessoaRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Pessoa> buscarPeloCodigo(@PathVariable Long id) {
		return pessoaRepository.findById(id).map(resp -> ResponseEntity.ok(resp))
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Pessoa> criar(@Valid @RequestBody Pessoa pessoa, HttpServletResponse response) {
		Pessoa pessoaSalva = pessoaRepository.save(pessoa);
		// Chamando o Evento
		publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoaSalva.getCodigo()));
		return ResponseEntity.status(HttpStatus.CREATED).body(pessoaSalva);

	}

	@PutMapping("/{id}")
	public ResponseEntity<Pessoa> atualizar(@PathVariable Long id, @Valid @RequestBody Pessoa pessoa) {
		return ResponseEntity.ok(pessoaService.atualizar(id, pessoa));
	}
	
	//Atualizando Apenas um atributo do objeto pessoa
	@PutMapping("/{codigo}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
		pessoaService.atualizarPropriedadeAtivo(codigo, ativo);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable Long id) {
		pessoaRepository.deleteById(id);
	}
}
