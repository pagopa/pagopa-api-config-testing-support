locals {
  count     = var.env_short == "d" ? 1 : 0
  repo_name = "pagopa-api-config-testing-support"

  display_name = "API Config testing support"
  description  = "API per interagire con la configurazione,di supporto ai test"
  path  = "apiconfig-testing-support/api"

  host         = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname     = var.hostname
}

resource "azurerm_api_management_group" "api_group" {
  count               = local.count
  name                = local.apim.product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.display_name
  description         = local.description
}

resource "azurerm_api_management_api_version_set" "api_version_set" {
  count               = local.count
  name                = format("%s-${local.repo_name}", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.display_name
  versioning_scheme   = "Segment"
}

module "api_v1" {
  count  = local.count
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.7.0"

  name                  = format("%s-${local.repo_name}", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = true

  version_set_id = azurerm_api_management_api_version_set.api_version_set[0].id
  api_version    = "v1"

  description  = local.description
  display_name = local.display_name
  path         = local.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
}
