FROM clojure:tools-deps AS builder

WORKDIR /build

# Install dependencies
COPY deps.edn build.clj ./
RUN clojure -P -T:build uber && \
  clojure -P -M -m com.tylerkindy.synchro.main

# Build
COPY src/ src
COPY resources/ resources
RUN clojure -T:build uber


FROM eclipse-temurin:24-jre

COPY --from=builder /build/target/synchro.jar synchro.jar

CMD ["java", "-jar", "synchro.jar"]
