^XA
^PW600^LL120
^CF0,60
^FO20,20^GB48,60,1^FS        ; expected cell box
^FO20,20^GB1,60,3^FS         ; left guard
^FO68,20^GB1,60,3^FS         ; 20 + 48 = 68 (right guard)
^FO20,20^FDM^FS              ; a wide glyph

^FO20,80^GB160,240,1^FS              ; expected cell box (160×240)
^FO20,80^GB1,240,3^FS                ; left guard
^FO180,80^GB1,240,3^FS               ; right guard (20 + 160)
^FO20,80^A0N,240,160^CI27^FDMM^FS     ; draw 'M' with explicit width/height
^XZ
