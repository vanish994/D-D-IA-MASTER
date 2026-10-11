# RPG Solo D&D 2024 — A Cripta

Protótipo inicial de RPG solo para Android, feito com Godot 4.x e pensado para quem está começando a programar.

## O que já existe

- Criação de personagem com nome e três arquétipos iniciais: Guerreiro, Ladino e Clérigo.
- Uma cena curta de aventura.
- Combate por turnos contra um goblin.
- Ataque, dano, pontos de vida e poção de cura.
- Recompensa por vitória.
- Salvamento e carregamento local em JSON.

## Como abrir

1. Instale o Godot 4.x no dispositivo ou computador.
2. Baixe ou clone este repositório.
3. No Godot, importe a pasta que contém o arquivo project.godot.
4. Execute a cena principal.

## Controles

Toque nos botões da tela. A interface é vertical e usa controles de UI que podem ser tocados.

## Salvamento

O jogo grava em user://solo_rpg_save.json, no armazenamento privado do aplicativo Godot. O salvamento é local ao dispositivo; enviar o projeto ao GitHub não envia automaticamente o progresso do jogo.

## Escopo e regras

Este é um protótipo educativo e simplificado, inspirado em fantasia de RPG. As estatísticas e regras ainda não representam uma implementação completa ou oficialmente validada das regras de D&D 2024.

## Próximos passos

1. Testar importação e execução no Godot Android.
2. Ajustar erros que o editor mostrar.
3. Melhorar regras de combate e opções de exploração.
4. Adicionar inventário e ficha mais detalhada.
5. Exportar e testar um APK Android.
