package bi.accounting.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("credits.transaction")
class CreditsConfig {

    var rate: Map<String, Double> = HashMap()
}