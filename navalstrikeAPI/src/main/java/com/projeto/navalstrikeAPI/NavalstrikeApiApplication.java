package com.projeto.navalstrikeAPI;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*  Batalha Naval com Fog of War — multiplayer, cada jogador só enxerga o próprio tabuleiro e o resultado dos tiros que dá no adversário.
  Requisitos:
  • Tabuleiro 10x10, frota padrão (5 navios: 5, 4, 3, 3, 2)
  • Multiplayer com 2 jogadores, turnos alternados
  • Servidor autoritativo — em hipótese alguma o cliente pode receber o estado do tabuleiro do oponente
  • Resultado dos tiros: erro, acerto ou afundado (revelando o tipo do navio)
  • Cadastro e login de usuários
  • Aplicação publicada (deploy) e acessível para jogar online
  • Testes automatizados cobrindo as regras de domínio
  • README com stack escolhida, arquitetura e justificativas*/
@SpringBootApplication
public class NavalstrikeApiApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(NavalstrikeApiApplication.class, args);
	}

}
