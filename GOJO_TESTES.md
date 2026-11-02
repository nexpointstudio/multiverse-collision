# Gojo: implementação mecânica provisória

Os números são valores iniciais. Custos, raios, duração, regeneração, PvP e destruição de terreno ficam em `world/serverconfig/multiversecollision-server.toml` após iniciar um mundo. Compras de habilidades usam XP Gojo, independente do XP vanilla. Use `/gojo status` e `/gojo buy <habilidade>`; os nomes são `infinity`, `blue`, `reverse`, `red`, `purple`, `amplified`, `domain`.

| Técnica | Compra (XP) | Energia | Recarga | Alcance, raio ou duração |
| --- | ---: | ---: | ---: | --- |
| Infinito (G) | 20 | 0,25/s | 0 | contínuo |
| Azul (segurar Z) | 30 | 5 + 3/s | 5 s | raio 6; 2 s após soltar |
| Energia Reversa (C) | 30 | 25 | 5 s | cura 8 HP |
| Vermelho (X) | 45 | 20 | 4 s | trajetória 24 blocos |
| Roxo (V) | 70 | 55 | 15 s | trajetória 36 blocos |
| Roxo ampliado (Shift+V) | 100 | 90 | 30 s | raio 12 |
| Domínio (B) | 120 | 80 | 60 s | raio 12; 8 s |

Energia máxima 100 e regeneração 2/s sem Infinito ou Azul sustentado. A percepção automática tem alcance inicial de 16 blocos e atualiza 2 vezes/s. Pré-requisitos: Vermelho requer Energia Reversa; Roxo requer Azul e Vermelho; Roxo ampliado requer Roxo; Domínio requer Roxo e Infinito.

## Comandos de operador

`/gojo debug xp <jogador> <quantidade>`, `/gojo debug energy <jogador> <0..100>` e `/gojo debug unlock <jogador> <habilidade>`. O desbloqueio de depuração ignora os pré-requisitos de compra para facilitar testes.

## Roteiro de teste manual pendente

1. Entrar em um mundo descartável com Seis Olhos e verificar `/gojo status`, HUD e falta de acesso antes da compra. Repetir os comandos sem o item.
2. Derrotar mobs diferentes e conferir XP. Comprar na ordem da árvore; tentar Roxo antes dos pré-requisitos. Desconectar, reconectar e morrer para conferir persistência.
3. Ligar G e testar ataque corpo a corpo, flechas, explosão, fogo, lava, afogamento, efeito negativo, empurrão e esgotamento de energia. Retirar o item durante o uso.
4. Segurar Z, mover a mira e o personagem, observar mobs, itens, projéteis e blocos simples. Soltar Z e conferir a curta duração e o assentamento dos blocos. Repetir com terreno desativado no serverconfig.
5. Disparar X e V contra mob e parede; conferir trajetória, dano, repulsão e blocos. Usar Shift+V com vários mobs e terreno simples.
6. Usar C ferido e com vida cheia, depois testar recarga e energia insuficiente.
7. Usar B com mobs dentro e fora do raio, durante entrada/saída, e conferir que voltam a agir após oito segundos. Testar em dois clientes com PvP desligado e ligado, respeitando as regras do servidor.
8. Testar limites de terreno em área protegida por mod de claims; essa integração precisa de validação específica.

## Limitações atuais

- A percepção usa texto na barra de ação; não há HUD gráfico. A seleção prioriza o monstro mais próximo, inclusive atrás de paredes.
- A proteção contra efeitos nocivos remove efeitos no próximo tick; um efeito instantâneo pode ocorrer antes da remoção.
- O domínio zera movimento e cancela ataques de entidades afetadas, mas não bloqueia toda ação possível de jogadores em PvP ou habilidades de outros mods.
- Blocos complexos, fluidos e indestrutíveis não são convertidos. A compatibilidade com plugins de proteção depende de seus eventos Forge; claims de outros mods exigem teste no servidor alvo.
- O XP impede pagamento duplicado pela mesma entidade morta; farms de invocações externas ainda exigem regras específicas.
- Build não equivale a testes dentro do Minecraft. Os itens deste roteiro permanecem **NÃO TESTADOS** até execução real.
