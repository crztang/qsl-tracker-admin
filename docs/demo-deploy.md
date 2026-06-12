# QSL Tracker Demo Deployment

This repo uses Docker Compose plus GitHub Actions to run a Linux demo environment.

## Images

- Backend: `ghcr.io/<owner>/qsl-tracker-admin`
- Frontend: `ghcr.io/<owner>/qsl-tracker-web`

GitHub Actions builds both images on every push to `main` or `demo`, then pushes them to GHCR.

## First Deployment

1. Copy `.env.demo.example` to `.env.demo` and fill in values.
2. Make sure Docker and Docker Compose are installed on the Linux server.
3. Log in to GHCR on the server if the images are private:

```bash
docker login ghcr.io -u <github-username> -p <personal-access-token-with-read:packages>
```

4. Start the stack:

```bash
docker compose -f docker-compose.demo.yml --env-file .env.demo up -d
```

The MySQL container imports `docs/database.sql` automatically on the first start when the data volume is empty.

## Update Flow

After code is pushed to GitHub:

1. GitHub Actions builds fresh images.
2. The server pulls the new images.
3. Restart the stack:

```bash
docker compose -f docker-compose.demo.yml --env-file .env.demo pull
docker compose -f docker-compose.demo.yml --env-file .env.demo up -d
```

## Rollback

Use the previous SHA tag instead of `latest`:

```bash
IMAGE_TAG=<previous-sha> docker compose -f docker-compose.demo.yml --env-file .env.demo up -d
```

## Notes

- The public entry point is the gateway Nginx container.
- `/` serves the Vue app.
- `/api` and Swagger endpoints proxy to the Spring Boot service.
- File uploads are stored under `/data/qsl-tracker/files` inside the backend container and persisted with a Docker named volume by default.
