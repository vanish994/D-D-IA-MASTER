# RPG Solo D&D 2024 — A Cripta

Protótipo de RPG solo para Android, migrando para **Kotlin + Jetpack Compose**. O objetivo é desenvolver e compilar o APK usando o GitHub, inclusive pelo celular Android, sem precisar editar cenas num motor de jogos.

## Estado atual

- Aplicativo Android nativo com interface de toque.
- Criação de aventureiro: Guerreiro, Ladino ou Clérigo.
- Ficha básica com pontos de vida, classe, CA e bônus de ataque.
- Aventura curta e combate por turnos contra um goblin.
- Rolagem de d20, ataques, dano e poção de cura.
- Salvamento local simples no aparelho.
- GitHub Actions compila um APK de depuração automaticamente.

## Como obter o APK

1. Abra a aba **Actions** deste repositório no GitHub.
2. Selecione **Build Android APK**.
3. Toque em **Run workflow** e confirme, ou aguarde o build automático após uma alteração.
4. Quando o processo terminar com sucesso, abra a execução e baixe o artefato **RPG-Solo-DnD-Android-APK**.
5. Extraia o ZIP e instale o arquivo APK no Android. O sistema pode pedir autorização para instalar apps dessa origem.

## Tecnologia

- Kotlin para as regras e o estado do jogo.
- Jetpack Compose para a interface Android.
- Gradle e GitHub Actions para compilar o APK.

Os arquivos antigos do protótipo Godot continuam no repositório por enquanto, como referência. A compilação Android agora usa a pasta `app/` e o projeto Gradle.

## Importante

Esta versão é um protótipo jogável com regras simplificadas, não uma implementação completa nem oficialmente validada de D&D 2024. O salvamento é local ao aparelho e ainda é básico. O APK só estará pronto para instalar depois que o workflow do GitHub terminar com sucesso.
