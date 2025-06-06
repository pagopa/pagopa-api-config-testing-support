locals {
  count     = var.env_short == "d" ? 1 : 0
  project_name = "api-config-testing-support"

  display_name = "API Config - Testing Support"
  description  = "API to support test activities in order to interact with Nodo configuration"
  path  = "apiconfig/testing-support"

  host         = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname     = var.hostname
}

#resource "azurerm_api_management_group" "api_group" {
#  count               = local.count
#  name                = local.apim.product_id
#  resource_group_name = local.apim.rg
#  api_management_name = local.apim.name
#  display_name        = "${local.display_name}"
#  description         = local.description
#}

################
### POSTGRES ###
################

resource "azurerm_api_management_api_version_set" "api_version_set_postgres" {
  count               = local.count
  name                = "${var.prefix}-${var.env_short}-${local.project_name}-p"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = "${local.display_name} / Postgres"
  versioning_scheme   = "Segment"
}

module "api_v1_postgres" {
  count  = local.count
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.7.0"

  name                  = "${var.prefix}-${var.env_short}-${local.project_name}-p"
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = true

  version_set_id = azurerm_api_management_api_version_set.api_version_set_postgres[0].id
  api_version    = "v1"

  description  = "${local.description} / Postgres version"
  display_name = "${local.display_name} / Postgres"
  path         = "${local.path}/p"
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
    version = "p"
  })
}

################
###  ORACLE  ###
################

resource "azurerm_api_management_api_version_set" "api_version_set_oracle" {
  count               = 0
  name                = "${var.prefix}-${var.env_short}-${local.project_name}-o"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = "${local.display_name} / Oracle"
  versioning_scheme   = "Segment"
}

module "api_v1_oracle" {
  count  = 0
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.7.0"

  name                  = "${var.prefix}-${var.env_short}-${local.project_name}-o"
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = true

  version_set_id = azurerm_api_management_api_version_set.api_version_set_oracle[0].id
  api_version    = "v1"

  description  = "${local.description} / Oracle version"
  display_name = "${local.display_name} / Oracle"
  path         = "${local.path}/o"
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
    version = "o"
  })
}
