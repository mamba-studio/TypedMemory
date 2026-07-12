from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    PageBreak,
    Preformatted,
)


OUT = "output/pdf/typedmemory-shapes-and-unions.pdf"


def styles():
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "Title",
            parent=base["Title"],
            fontName="Helvetica-Bold",
            fontSize=24,
            leading=29,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#1f2937"),
            spaceAfter=14,
        ),
        "subtitle": ParagraphStyle(
            "Subtitle",
            parent=base["Normal"],
            fontName="Helvetica",
            fontSize=11,
            leading=15,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#4b5563"),
            spaceAfter=24,
        ),
        "h1": ParagraphStyle(
            "Heading1",
            parent=base["Heading1"],
            fontName="Helvetica-Bold",
            fontSize=15,
            leading=19,
            textColor=colors.HexColor("#111827"),
            spaceBefore=16,
            spaceAfter=7,
        ),
        "h2": ParagraphStyle(
            "Heading2",
            parent=base["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=12,
            leading=15,
            textColor=colors.HexColor("#374151"),
            spaceBefore=10,
            spaceAfter=5,
        ),
        "body": ParagraphStyle(
            "Body",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=9.7,
            leading=13.5,
            textColor=colors.HexColor("#1f2937"),
            spaceAfter=6,
        ),
        "small": ParagraphStyle(
            "Small",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=8.4,
            leading=11,
            textColor=colors.HexColor("#374151"),
        ),
        "code": ParagraphStyle(
            "Code",
            fontName="Courier",
            fontSize=7.4,
            leading=9.2,
            textColor=colors.HexColor("#111827"),
            backColor=colors.HexColor("#f3f4f6"),
            borderColor=colors.HexColor("#d1d5db"),
            borderWidth=0.4,
            borderPadding=5,
            spaceBefore=4,
            spaceAfter=9,
        ),
    }


S = styles()


def p(text, style="body"):
    return Paragraph(text, S[style])


def code(text):
    return Preformatted(text.strip("\n"), S["code"])


def page_num(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#6b7280"))
    canvas.drawRightString(A4[0] - 0.55 * inch, 0.42 * inch, f"TypedMemory design note - {doc.page}")
    canvas.restoreState()


def coverage_table():
    rows = [
        ["Case", "Shape API", "Representation idea"],
        ["Static union branch", ".union(...).variant(...)", "One known concrete variant"],
        ["Union root", "MemShapes.union(...)", "Shape starts at a sealed interface"],
        ["Sibling tag", ".taggedUnion(tag, tagType, payload, ...)", "One record owns tag and payload fields"],
        ["Tagged array slots", ".taggedUnionArray(...)", "Each element owns tag and payload fields"],
        ["Overlay-derived tag", ".union(...).overlay().tagFrom(..., tagType)", "C union bytes, tag read through one variant layout"],
        ["Region focus", "RegionPath / MemPaths", "Where the bytes are"],
    ]
    table = Table(rows, colWidths=[1.45 * inch, 2.0 * inch, 2.7 * inch], hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#111827")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8.2),
                ("LEADING", (0, 0), (-1, -1), 10.5),
                ("FONTNAME", (0, 1), (-1, -1), "Helvetica"),
                ("BACKGROUND", (0, 1), (-1, -1), colors.HexColor("#f9fafb")),
                ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#d1d5db")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
            ]
        )
    )
    return table


story = []
story.append(p("TypedMemory Shapes, Paths, And Union Representations", "title"))
story.append(
    p(
        "A short design note capturing the path we explored: records and sealed interfaces as the type model, "
        "memory regions as the representation model, and MemShape/RegionPath as the bridge between them.",
        "subtitle",
    )
)

story.append(p("1. The Core Split", "h1"))
story.append(
    p(
        "The important separation is not between Java and native memory. It is between type, region, and interpretation. "
        "Once these are separate, many low-level C/OpenCL layout idioms become expressible without giving up the Java record model."
    )
)
story.append(
    code(
        """
type model       -> records, sealed interfaces, variants
memory region    -> byte offset, layout, array coordinates
interpretation   -> path, shape, tag rule, overlay rule
"""
    )
)
story.append(
    p(
        "RegionPath answers: where is the memory region? MemShape answers: how should union choices under that region be interpreted?"
    )
)

story.append(p("2. RegionPath: Structural Lensing", "h1"))
story.append(
    p(
        "A RegionPath is a validated structural key. If two separately built paths describe the same root, tokens, and leaf, "
        "they compare equal and can be used as cache keys."
    )
)
story.append(
    code(
        """
record Pixel(int i, int j) {}
record Point(float x, float y, Pixel p) {}

var p1 = MemPaths.from(Point.class)
        .field("p", Pixel.class)
        .region();

var p2 = MemPaths.from(Point.class)
        .field("p", Pixel.class)
        .region();

assert p1.equals(p2);
"""
    )
)
story.append(
    p(
        "Arrays add coordinates to the path. Fixed indexes close the coordinate. Open indexes become handle coordinates later."
    )
)
story.append(
    code(
        """
record Palette(@size(4) Pixel[] pixels) {}

var fixed = MemPaths.from(Palette.class)
        .array("pixels", Pixel.class)
        .at(2)
        .region();

var open = MemPaths.from(Palette.class)
        .array("pixels", Pixel.class)
        .any()
        .region();
"""
    )
)

story.append(PageBreak())
story.append(p("3. MemShape: Concrete Union Interpretation", "h1"))
story.append(
    p(
        "MemShape describes which concrete variants are used under a record or union root. It validates completeness while building, "
        "so missing union choices are caught before generated handles are involved."
    )
)
story.append(
    code(
        """
record DeviceRoot(Device device) {}
sealed interface Device permits SmartDevice, SimpleDevice {}
record SmartDevice(Profile profile) implements Device {}
record SimpleDevice(int value) implements Device {}

var shape = MemShapes.of(DeviceRoot.class)
        .union("device", Device.class, device -> device
            .variant(SmartDevice.class, smart -> smart
                .field("profile", Profile.class, profile -> ...)))
        .shape();
"""
    )
)
story.append(
    p(
        "A shape can also start at a union root when the region being decoded is itself declared as a sealed interface."
    )
)
story.append(
    code(
        """
var shape = MemShapes.union(Device.class)
        .variant(SmartDevice.class, smart -> smart
            .field("profile", Profile.class, profile -> ...))
        .shape();
"""
    )
)

story.append(p("4. Sibling Union Fields", "h1"))
story.append(
    p(
        "A record can contain multiple independent union fields. MemShape is not a single chain; it stores sibling branches by field name."
    )
)
story.append(
    code(
        """
record Root(Left left, Right right) {}

var shape = MemShapes.of(Root.class)
        .union("left", Left.class, left -> left
            .variant(A.class, a -> a
                .union("child", AChild.class, child -> child
                    .variant(AChild1.class))))
        .union("right", Right.class, right -> right
            .variant(D.class, d -> d
                .union("mode", Mode.class, mode -> mode
                    .variant(ModeX.class))))
        .shape();
"""
    )
)

story.append(p("5. Tagged Union: Sibling Tag + Payload", "h1"))
story.append(
    p(
        "This matches a common systems layout: a record owns a discriminator field and a union payload field. "
        "The tag is outside the payload but in the same record."
    )
)
story.append(
    code(
        """
sealed interface LightPayload permits AreaLight, IblLight, SpotLight {}

record Light(int type, LightPayload payload, float multiplier) {}
record LightTag(int type) {
    static LightTag fromNative(int raw) {
        return new LightTag(raw);
    }
}
record AreaLight(int id, int shapeidx, int primidx, int padding) implements LightPayload {}
record IblLight(int tex, int texReflection, int texRefraction, int texTransparency) implements LightPayload {}
record SpotLight(float ia, float oa, float f, int padding) implements LightPayload {}

var shape = MemShapes.of(Light.class)
        .taggedUnion(
                TagAdapter.ofInt("type", LightTag.class, LightTag::fromNative),
                "payload",
                LightPayload.class,
                light -> light
                    .caseOf(new LightTag(1), AreaLight.class)
                    .caseOf(new LightTag(2), IblLight.class)
                    .caseOf(new LightTag(3), SpotLight.class))
        .shape();
"""
    )
)

story.append(PageBreak())
story.append(p("6. Tagged Union Array: Per-Element Tags", "h1"))
story.append(
    p(
        "If every array element can have a different active variant, the array element should be a tagged slot. "
        "A single uniform shape is not enough for mixed elements."
    )
)
story.append(
    code(
        """
record TaggedBatch(TaggedDevice[] devices) {}
record TaggedDevice(int tag, Device payload) {}
record DeviceTag(int bucket) {
    static DeviceTag fromNative(int raw) {
        return new DeviceTag(raw > 1 ? 2 : 1);
    }
}

var shape = MemShapes.of(TaggedBatch.class)
        .taggedUnionArray(
                "devices",
                TaggedDevice.class,
                TagAdapter.ofInt("tag", DeviceTag.class, DeviceTag::fromNative),
                "payload",
                Device.class,
                devices -> devices
                    .caseOf(new DeviceTag(1), SmartDevice.class)
                    .caseOf(new DeviceTag(2), SimpleDevice.class))
        .shape();
"""
    )
)
story.append(
    p(
        "Supported native tag carriers are byte, short, int, long, boolean, and char. A TagAdapter can normalize them into a user-defined record tag before case equality is checked."
    )
)
story.append(
    code(
        """
sealed interface RangePayload permits SmallRange, LargeRange {}

record RangePacket(int rawCode, RangePayload payload) {}
record RangeTag(String bucket) {
    static RangeTag fromNative(int raw) {
        return new RangeTag(raw <= 22 ? "small" : "large");
    }
}
record SmallRange(int value) implements RangePayload {}
record LargeRange(long value) implements RangePayload {}

var shape = MemShapes.of(RangePacket.class)
        .taggedUnion(
                TagAdapter.ofInt("rawCode", RangeTag.class, RangeTag::fromNative),
                "payload",
                RangePayload.class,
                packet -> packet
                    .caseOf(new RangeTag("small"), SmallRange.class)
                    .caseOf(new RangeTag("large"), LargeRange.class))
        .shape();
"""
    )
)

story.append(p("7. Overlay Union: C-Style Shared Storage", "h1"))
story.append(
    p(
        "Some C/OpenCL unions are not tag + payload. They are one storage region with several overlay views. "
        "The InputMapData example is this form: the tag is found by viewing the same bytes as one particular variant."
    )
)
story.append(
    code(
        """
// OpenCL shape:
typedef struct _InputMapData {
    union {
        struct { float3 value; } float_value;
        struct { int idx; int placeholder[2]; int type; } int_values;
    };
} InputMapData;
"""
    )
)
story.append(
    code(
        """
sealed interface InputMapUnion permits FloatValue, IntValues {}

record InputMapData(InputMapUnion u) {}
record Float3(float x, float y, float z) {}
record FloatValue(Float3 value) implements InputMapUnion {}
record IntValues(int idx, int placeholder0, int placeholder1, int type) implements InputMapUnion {}
record InputMapTag(int type) {
    static InputMapTag fromNative(int raw) {
        return new InputMapTag(raw);
    }
}

var shape = MemShapes.of(InputMapData.class)
        .union("u", InputMapUnion.class, u -> u
            .overlay()
            .tagFrom(IntValues.class, TagAdapter.ofInt("type", InputMapTag.class, InputMapTag::fromNative))
            .caseOf(new InputMapTag(0), FloatValue.class)
            .caseOf(new InputMapTag(2), IntValues.class))
        .shape();
"""
    )
)
story.append(
    p(
        "tagFrom(IntValues.class, TagAdapter.ofInt(...)) means: interpret the union bytes as IntValues, then normalize the native int type field into the user tag record."
    )
)

story.append(p("8. How The Pieces Compose", "h1"))
story.append(
    p(
        "The future handle/view layer can compose a RegionPath and a MemShape. The path locates the bytes. "
        "The shape says how to realize sealed-interface values and nested variants."
    )
)
story.append(
    code(
        """
Device d = mem.handle(devicePath)
        .view(deviceShape)
        .get(rootIndex, deviceIndex);
"""
    )
)
story.append(
    p(
        "For shaped reads, tag checks should be part of get(). A lower-level matches() can exist, but pattern matching and shaped decoding should be the nice public path."
    )
)

story.append(PageBreak())
story.append(p("9. Coverage Summary", "h1"))
story.append(coverage_table())
story.append(Spacer(1, 12))
story.append(
    p(
        "This does not claim to cover every C layout trick forever. It does cover the major practical families found in record/sealed type models, "
        "GPU payload layouts, tag + payload slots, mixed union arrays, and overlay-derived discriminators."
    )
)


doc = SimpleDocTemplate(
    OUT,
    pagesize=A4,
    rightMargin=0.62 * inch,
    leftMargin=0.62 * inch,
    topMargin=0.58 * inch,
    bottomMargin=0.62 * inch,
)

doc.build(story, onFirstPage=page_num, onLaterPages=page_num)
print(OUT)
