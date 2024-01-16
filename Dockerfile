FROM clojure:tools-deps AS builder

WORKDIR /build
COPY . .
RUN clojure -T:build uber


FROM eclipse-temurin:24-jre

COPY --from=builder /build/target/synchro.jar synchro.jar

EXPOSE 80
CMD ["java", "-jar", "synchro.jar"]
