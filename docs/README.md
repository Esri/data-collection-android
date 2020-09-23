# Data Collection for Android documentation

<!-- MDTOC maxdepth:6 firsth1:0 numbering:0 flatten:0 bullets:1 updateOnSave:1 -->

- [Description](#description)   
   - [Generic application](#generic-application)   
   - [Portland Tree Survey](#portland-tree-survey)   
- [Using the app](#using-the-app)   
   - [Manage the app's context](#manage-the-apps-context)   
      - [Sign in and out of portal](#sign-in-and-out-of-portal)   
   - [Identify map features](#identify-map-features)   
   - [View data with pop-ups](#view-data-with-pop-ups)   
- [Using web maps](#using-web-maps)   
   - [Configure web map & feature services for Data Collection](#configure-web-map-feature-services-for-data-collection)   
      - [Map title](#map-title)   
      - [Organizing feature layers](#organizing-feature-layers)   
      - [Feature layer visibility range](#feature-layer-visibility-range)   
      - [Enable editing on feature layers and tables](#enable-editing-on-feature-layers-and-tables)   
      - [Enable pop-up on feature layers and tables](#enable-pop-up-on-feature-layers-and-tables)   
      - [Configure pop-up on feature layers and tables](#configure-pop-up-on-feature-layers-and-tables)   
- [Authentication model](#authentication-model)   
- [Using map definition & pop-up configurations to drive app behavior](#using-map-definition-pop-up-configurations-to-drive-app-behavior)   
   - [Map identify rules](#map-identify-rules)   

<!-- /MDTOC -->
---

## Description

Collect data in an app consuming your organization's web map driven by the ArcGIS Web GIS information model. Use the example _Portland Tree Survey_ web map and dataset to get started.

### Generic application

The app was designed to work in a generic context and thus your organization can configure the app to consume your own web map, out of the box. To accomplish this, the web map is configured by a set of rules that the app adheres to, driving the app's behavior. These rules are defined by the map's definition and by the map's layers' pop-up configurations. To learn more about what drives the app's behavior, read the section entitled [_Using map definition & pop-up configurations to drive app behavior_](#using-map-definition--pop-up-configurations-to-drive-app-behavior).

### Portland Tree Survey

The capabilities of the app can be demonstrated using *Portland Tree Survey*, a web map hosted and maintained by the Esri ArcGIS Runtime organization that ships with the app by default. *Portland Tree Survey* tells the story of a city arborist or engaged citizens who maintains inspections for all street trees in the city of Portland, OR.

Users can identify existing trees on a map.

## Using the app

Data Collection is a map-centric application in that it launches to a view containing a map.

![Main Map View](/docs/images/anatomy-map-view.png)

The app bar's title reflects the name of the web map and the app bar button items are as follows:

| Icon | Description |
| ---- | ----------- |
| ![Navigation Drawer](/docs/images/navigation-drawer.png) | Navigation drawer toggle button to reveal the navigation drawer view. |

### Manage the app's context

Tapping the app bar's drawer button reveals the app context drawer view in the navigation drawer.

![App Context Drawer View](/docs/images/anatomy-app-context-navigationdrawer.png)

#### Sign in and out of portal

Upon first launch, the user is not authenticated and the app does not prompt for authentication. To sign in, the user can tap the navigation bar's navigation button to reveal the app context drawer view. Once revealed, the user can tap 'Log in'. A modal login view presents, prompting for the user's portal username and password. If valid credentials are provided, an authenticated user is associated with the portal and their credentials are stored in the local credential cache. This credential cache is encrypted and stored in shared preferences.

Upon successfully signing in, the button that previously read 'Log in' now reads 'Log out'. Tapping the button now signs the user out and removes the user from the local credential cache.

### Identify map features

Tapping the map performs an identify function on the map. One best result is chosen, a small pop-up view is revealed in the bottomsheet and the feature is selected on the map.

![Identified Map Feature](/docs/images/anatomy-identified-feature.png)

### View data with pop-ups

After identifying a pop-up, tapping the small pop-up view modally presents that pop-up in a more detailed pop-up view.

A full screen `RecyclerView` based controller allows the user to interrogate the map view's selected pop-up in greater detail.

![View A Pop-up](/docs/images/anatomy-popup-view.png)

## Using web maps

You can author your own web maps in [Portal/ArcGIS Online](https://enterprise.arcgis.com/en/portal/latest/use/what-is-web-map.htm) or [ArcGIS Desktop](https://desktop.arcgis.com/en/maps/) and share them in your app via your Portal; this is the central power of the Web GIS model built into ArcGIS. Building an app which uses a web map allows the cartography and map configuration to be completed in Portal rather than in code. This then allows the map to change over time, without any code changes or app updates. Learn more about the benefits of developing with web maps [here](https://developers.arcgis.com/web-map-specification/). Also, learn about authoring web maps in [Portal/ArcGIS Online](https://doc.arcgis.com/en/arcgis-online/create-maps/make-your-first-map.htm) and [ArcGIS Pro](https://pro.arcgis.com/en/pro-app/help/mapping/map-authoring/author-a-basemap.htm).

Loading web maps in code is easy; the app loads a web map from a portal (which may require the user to log in, see the [_Authentication model_](#authentication-model) section) with the following code:

``` kotlin
val portal = Portal(portalUrl, loginRequired)
val portalItem = PortalItem(portal, itemId)
mapView.map = ArcGISMap(portalItem)
```

### Configure web map & feature services for Data Collection

The app's behavior is configuration driven and the following configuration principles should guide you in the configuration of your **own** web map.

> Always remember to save your web map after changes have been performed!

#### Map title

The web map's title becomes the title of the map in the map view's navigation bar.

> A succinct, descriptive title is recommended because some screen sizes are quite small.

#### Organizing feature layers

The [order](https://doc.arcgis.com/en/arcgis-online/create-maps/organize-layers.htm) of your web map's [feature layers](https://doc.arcgis.com/en/arcgis-online/reference/feature-layers.htm) matter. Layer precedence is assigned to the top-most layer (index 0) first with the next precedence assigned to the next layer beneath, and so on. This is important because only one feature can be identified at a time. When the app performs an identify operation, the layer whose index is nearest 0 and which returns results is the one whose features will be selected.

#### Feature layer visibility range

It is generally recommended to consider the [visibility range](https://doc.arcgis.com/en/arcgis-online/create-maps/set-visibility.htm) of your feature layers. Beyond this general consideration, only visible layers are returned when an identify operation is performed. You'll want to consider which layers to make visible at what scale.

#### Enable editing on feature layers and tables

You'll want to consider whether to enable or disable [editing](https://doc.arcgis.com/en/arcgis-online/manage-data/edit-features.htm) of your feature layers and tables. Specifically, a user is only able to edit features or records on layers whose backing table has editing enabled. This includes related records for features. For instance, if a feature whose backing table does permit editing has a related record backed by a table that does not have editing enabled, that related record layer cannot be edited by the app.

#### Enable pop-up on feature layers and tables

The app relies on pop-up configurations to identify, view, and edit features and records. You'll want to consider whether to enable or disable [pop-ups](https://doc.arcgis.com/en/arcgis-online/create-maps/configure-pop-ups.htm#ESRI_SECTION1_9E13E02AABA74D5DA2DF1A34F7FB3C63) of your feature layers and tables. Only feature layers and tables that are pop-up-enabled can be identified, displayed, or edited. Please note, you can have a scenario where you've enabled editing on a layer (as described above) but have disabled pop-ups for the same layer and thus a user is not be able to edit this layer.

#### Configure pop-up on feature layers and tables

For all layers with pop-ups enabled, you'll want to consider how each layer's pop-up is [configured](https://doc.arcgis.com/en/arcgis-online/create-maps/configure-pop-ups.htm#ESRI_SECTION1_0505720B006E43C5B14837A353FFF9EC) for display and editing.

**Pop-up Title**

You can configure the pop-up title with a static string or formatted with attributes. The pop-up's title becomes the title of the navigation bar when displaying the pop-up. A succinct, descriptive title is recommended because some screen sizes are quite small.

**Pop-up Display**

It is recommended to configure your pop-ups such that their content's [display property](https://doc.arcgis.com/en/arcgis-online/get-started/view-pop-ups.htm) is set to **a list of field attributes**. Using this configuration allows you to designate the display order of that table's attributes. This is important because various visual representations of pop-ups in the app are driven by the attributes display order.

> With the Configure Pop-up pane open, under Pop-up Contents the display property provides a drop down list of options, select **a list of field attributes**.

**Pop-up Attributes**

Precedence is assigned to top-most attributes first (index 0) with the next precedence assigned to the subsequent attributes. Individual attributes can be configured as display, edit, both, or neither.

> With the Configure Attributes window open, attributes can be re-ordered using the up and down arrows.

Within the app, a pop-up view can be in display mode or edit mode and attributes configured as such are made available for display or edit.

These attributes' values are accompanied by a title label, which is configured by the attribute's field alias. It is recommended to configure the field alias with a label that is easily understood to represent what is contained by that field.

## Authentication model

The app leverages the ArcGIS [authentication](https://developers.arcgis.com/authentication/) model to provide access to resources via the [named user](https://developers.arcgis.com/documentation/core-concepts/security-and-authentication/#named-user-login) login pattern. When accessing services that require a named user, the app prompts you for your organization’s portal credentials used to obtain a token. The ArcGIS Runtime SDKs provide a simple-to-use API for dealing with ArcGIS logins.

The workflow of obtaining an `access_token` for accessing OAuth 2.0 secured services is illustrated in the following diagram.

![ArcGIS Authentication Model](/docs/images/authentication.jpg)

1. A challenge is issued to the app user when they try to access a secured portal by the `DefaultChallengeHandler` set
   on the `AuthenticationManager`.
2. If the user is successfully authenticated, they will then be redirected back to your application with the authorization code in the query string.
3. Your application exchanges the authorization code for an `access_token` from the token endpoint.
4. The `AuthenticationManager` stores the credential for this portal and all requests for secured content includes the access_token in the request.

The `AuthenticationManager` class takes care of steps 1-4 in the diagram above. For an application to use this pattern, follow these [guides](https://developers.arcgis.com/authentication/signing-in-arcgis-online-users/) to register your app.

``` kotlin
AuthenticationManager.addOAuthConfiguration(
            OAuthConfiguration(
                portalUrl,
                application.getString(R.string.oauth_client_id),
                application.getString(R.string.redirectUrl)
            )
        )
```

Any time a secured service issues an authentication challenge, the `DefaultAuthenticationChallengeHandler` and the app's `AuthenticationManager` work together to broker the authentication transaction. The `redirectUrl` above tells Android how to call back to the app to confirm authentication with the Runtime SDK.

Android routes the redirect URL through the `DefaultOAuthIntentReceiver` which the app passes directly to an ArcGIS Runtime SDK helper function to retrieve a token:

``` kotlin
 val handler = DefaultAuthenticationChallengeHandler(activity)
 AuthenticationManager.setAuthenticationChallengeHandler(handler)
```

When the user successfully authenticates, a URI is passed from the web browser control. The URI is decoded and passed back to the `AuthenticationManager` to retrieve the token. The Android app retrieves all the necessary information (`AppClientID` and `RedirectURL`) to set up the `AuthenticationManager` from the `app_settings.xml` file.

Note the value for `RedirectURL`. Combined with the text `auth` to make `data-collection://auth`, this is the [redirect URI](https://developers.arcgis.com/authentication/browser-based-user-logins/#configuring-a-redirect-uri) that you configured when you registered your app on your [developer dashboard](https://developers.arcgis.com/applications). For more details on the user authorization flow, see the [Authorize REST API](https://developers.arcgis.com/rest/users-groups-and-items/authorize.htm).

For more details on configuring the app for OAuth, see [the main README.md](/README.md).

## Using map definition & pop-up configurations to drive app behavior

The app operates on a set of rules driven by map definitions and pop-up configurations. To learn how to configure your web map, see the section entitled [_Configure web map & feature services for Data Collection_](#configure-web-map--feature-services-for-data-collection).

### Map identify rules

A tap gesture on the map view performs an identify function where only results for layers that adhere to certain rules are considered. These rules ask that the layer is visible, is of point type geometry, and has pop-ups enabled.

```kotlin
val identifiableLayer: FeatureLayer?
        get() {
            return map.value?.operationalLayers?.filterIsInstance<FeatureLayer>()?.filter {
                (it.featureTable?.geometryType == GeometryType.POINT)
                    .and(it.isVisible)
                    .and(it.isPopupEnabled && it.popupDefinition != null)
            }?.get(0)
        }
```
