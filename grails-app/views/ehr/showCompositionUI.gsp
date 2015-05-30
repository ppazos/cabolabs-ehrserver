<html>
  <head>
    <style>
    .single_value {
      background-color: #99aaff;
    }
    
    /* ------------------------------>>>> Estilo general */
    body {
      margin: 0 1em;
    }
    
    /* Oculta datos de contexto */
    .composition_language,
    .composition_territory,
    .composition_category,
    .composition_content_language,
    .composition_content_encoding,
    .composition_content_subject,
    .composition_uid {
      display: none;
    }
    .composition_composer {
      font-size: 1.4em;
      padding: 0.3em;
      background-color: #ffffcc;
    }
    .composition_context_start_time {
      font-size: 1.4em;
      padding: 0.3em;
      background-color: #ffcccc;
    }
    .composition_context_setting {
      font-size: 1.4em;
      padding: 0.3em;
      background-color: #aaaacc;
    }
    .composition_context_setting_defining_code {
      display: none;
    }
    .composition {
      position: relative;
      top: 0; /* Mueve todo para abajo o para arriba */
    }
    .composition_name {
      font-weight: bold;
    }
    .composition_name .single_value {
      background-color: #ffccaa;
    }
    .composition_content {
      position: relative;
      width: 100%;
      top: 5em;
      border: 1px solid #000;
    }
    .composition_content div {
      padding: 0 0 0 1em; /* muestra arbol de contenido */
    }
    /* ------------------------------>>>> /Estilo general */
    
    /* ------------------------------>>>> Estilo por tipo de dato */
    TODO:
    /* ------------------------------>>>> /Estilo por tipo de dato */
    </style>
  </head>
  <body>
    ${compositionHtml}
  </body>
</html>