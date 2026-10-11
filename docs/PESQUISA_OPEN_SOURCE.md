# Pesquisa: projetos open source para o RPG solo

Pesquisa inicial feita em outubro de 2026. Os projetos abaixo são referências e opções; ainda não foram adicionados como dependências do jogo.

## Recomendações

### 1. Godot Open RPG — referência principal de código
- Repositório: https://github.com/gdquest-demos/godot-open-rpg
- Licença indicada no repositório: MIT.
- O que pode ensinar: estrutura de RPG por turnos, combate, inventário, progressão, diálogos e transições.
- Atenção: o README atual exige Godot 4.6.2 e declara que o projeto está em desenvolvimento. Não copiar o projeto inteiro para o nosso jogo; usar como referência e adaptar partes pequenas depois de confirmar a versão do Godot instalada.

### 2. Dialogic 2 — diálogos e cenas narrativas
- Repositório: https://github.com/dialogic-godot/dialogic
- Licença: MIT.
- O que pode ajudar: criar conversas com personagens e narrativas ramificadas sem programar todo o editor de diálogos do zero.
- Atenção: a versão atual requer Godot 4.5 ou superior. Não instalar até confirmarmos a versão do editor e até o protótipo básico estar funcionando.

### 3. CSheet — referência para regras e ficha de D&D
- Repositório: https://github.com/igor47/csheet
- O que oferece: aplicativo open source de ficha de personagem de D&D 5e que informa suporte às regras SRD 5.1 (2014) e SRD 5.2 (2024).
- Como usar com responsabilidade: consultar a organização dos dados e as regras cobertas; não presumir que seja um plugin Godot ou que seu código possa ser transplantado diretamente. Verificar licença e arquivos específicos antes de reutilizar código ou conteúdo.

### 4. Inventory Forge — possível ferramenta futura de itens
- Repositório: https://github.com/Menkos/inventory-forge
- Licença indicada: MIT.
- O que pode ajudar: organizar itens, tabelas de saque e dados de inventário no editor Godot.
- Atenção: é uma ferramenta adicional; deixar para uma etapa posterior, quando já houver inventário no protótipo.

### 5. Godot RPG Framework — estudar com cautela
- Repositório: https://github.com/SucceedHQ-innovations/godot-rpg-framework
- Licença indicada: MIT.
- O README anuncia combate, diálogos, inventário e missões, mas o repositório consultado tinha histórico muito curto. Tratar como experimento para leitura, não como base confiável para o projeto sem revisão adicional.

## Decisão para este protótipo

1. **Não importar frameworks agora.** Primeiro, abrir e executar o nosso projeto no Godot e corrigir erros.
2. Usar o Godot Open RPG como material de estudo para melhorar a arquitetura do combate.
3. Quando o fluxo básico estiver estável, avaliar Dialogic para narrativas e falas.
4. Usar CSheet como referência de ficha e regras SRD 2024, conferindo licenças antes de reutilizar qualquer conteúdo.
5. Adicionar uma dependência por vez e registrar versão e licença em THIRD_PARTY_NOTICES.md.

## Compatibilidade Android

O editor Godot para Android existe, mas tem limitações e está em acesso antecipado conforme a documentação. A documentação oficial recomenda sincronização Git por cliente Android/Termux. Evitar plugins com extensões nativas até confirmar suporte Android e a versão do motor.

- Documentação do editor Android: https://docs.godotengine.org/pt-br/4.5/tutorials/editor/using_the_android_editor.html

## Observação sobre D&D

O protótipo atual é uma implementação educativa simplificada. Antes de reproduzir regras, monstros, feitiços ou texto oficial, verificar os termos e o conteúdo da SRD 5.2 de 2024 e manter créditos/licenças exigidos. Não presumir que todo material publicado por fãs ou em ferramentas de ficha esteja liberado para reutilização.
