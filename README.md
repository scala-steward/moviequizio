# MovieQuiz.io

Daily movie guessing challenge.

Built with [Scala.js](https://www.scala-js.org/)

[![Main](https://github.com/ghelouis/moviequizio/actions/workflows/main.yml/badge.svg)](https://github.com/ghelouis/moviequizio/actions/workflows/main.yml)

[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

## Local development

### Compile Scala code to JavaScript

```
sbt ~fastLinkJS
```

### Play the game

Open [index.html](./index.html) in your favourite browser

### Run tests

```
sbt test
```

### Linting

- [Prettier](https://prettier.io/) for HTML/CSS
- [Scalafmt](https://scalameta.org/scalafmt/) for Scala.

Check automatically on commit:

- Install [pre-commit](https://pre-commit.com/#installation) and copy [scripts/pre-commit](./scripts/pre-commit) to `.git/hooks/pre-commit`.

## Production

### Generate optimized JavaScript

```
sbt fullLinkJS
```

### Serve

MovieQuiz.io requires no backend server and can be hosted as a static website. Simply provide [index.html](./index.html), [favicon.ico](./favicon.ico), the [assets](./assets) directory and the generated JS file (typically in `target/scala-X.X.X/moviequiz-io-opt/main.js`). The path in [index.html](./index.html) may have to be adapted.

### Continuous deployment

The steps above are automated, every commit will trigger the main job to run and publish to https://moviequiz.io (or a preview URL on PRs).
