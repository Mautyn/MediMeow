package pl.edu.pk.student.feature_interactions.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrugSearchResponse(
    @SerialName("meta")
    val meta: MetaResponse? = null,
    @SerialName("results")
    val results: List<DrugLabelResponse>? = null
)

@Serializable
data class MetaResponse(
    @SerialName("disclaimer")
    val disclaimer: String? = null,
    @SerialName("terms")
    val terms: String? = null,
    @SerialName("license")
    val license: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null,
    @SerialName("results")
    val resultsInfo: ResultsInfoResponse? = null
)

@Serializable
data class ResultsInfoResponse(
    @SerialName("skip")
    val skip: Int? = null,
    @SerialName("limit")
    val limit: Int? = null,
    @SerialName("total")
    val total: Int? = null
)

@Serializable
data class DrugLabelResponse(
    @SerialName("openfda")
    val openfda: OpenFdaInfoResponse? = null,
    @SerialName("drug_interactions")
    val drugInteractions: List<String>? = null,
    @SerialName("warnings")
    val warnings: List<String>? = null,
    @SerialName("adverse_reactions")
    val adverseReactions: List<String>? = null,
    @SerialName("indications_and_usage")
    val indicationsAndUsage: List<String>? = null,
    @SerialName("purpose")
    val purpose: List<String>? = null,
    @SerialName("warnings_and_cautions")
    val warningsAndCautions: List<String>? = null
)

@Serializable
data class OpenFdaInfoResponse(
    @SerialName("generic_name")
    val genericName: List<String>? = null,
    @SerialName("brand_name")
    val brandName: List<String>? = null,
    @SerialName("manufacturer_name")
    val manufacturerName: List<String>? = null,
    @SerialName("product_type")
    val productType: List<String>? = null,
    @SerialName("route")
    val route: List<String>? = null,
    @SerialName("substance_name")
    val substanceName: List<String>? = null,
    @SerialName("rxcui")
    val rxcui: List<String>? = null,
    @SerialName("spl_id")
    val splId: List<String>? = null,
    @SerialName("spl_set_id")
    val splSetId: List<String>? = null,
    @SerialName("package_ndc")
    val packageNdc: List<String>? = null,
    @SerialName("product_ndc")
    val productNdc: List<String>? = null,
    @SerialName("pharm_class_epc")
    val pharmClassEpc: List<String>? = null,
    @SerialName("pharm_class_pe")
    val pharmClassPe: List<String>? = null,
    @SerialName("pharm_class_moa")
    val pharmClassMoa: List<String>? = null
)