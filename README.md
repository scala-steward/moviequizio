# MovieQuiz.io

Daily movie guessing challenge.

Built with [Scala.js](https://www.scala-js.org/)

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
