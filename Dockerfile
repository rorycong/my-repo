# ── Stage 1: Build TypeScript Frontend ──────────────────────────────────────────
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ── Stage 2: Compile Java Backend ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /app

COPY src/ ./src/
# Copy built web assets from Stage 1
COPY --from=frontend-builder /app/src/main/resources/web/ ./src/main/resources/web/

# Compile all Java source files
RUN mkdir -p bin/main && \
    find src/main/java -name "*.java" > sources.txt && \
    javac -d bin/main @sources.txt && \
    cp -r src/main/resources/web bin/main/

# ── Stage 3: Minimal Production Runtime ───────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled classes and web resources
COPY --from=backend-builder /app/bin/main/ ./bin/main/
COPY --from=backend-builder /app/src/main/resources/web/ ./src/main/resources/web/

# Create data directory for file-based database persistence
RUN mkdir -p /app/data

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "bin/main", "splitter.server.ServerMain"]
