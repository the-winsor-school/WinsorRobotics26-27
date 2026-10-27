--[[
Build the field guide:  one Word file per chapter for Flint, or the whole
printed book.

    pandoc lua doc/FieldGuide/reference/build_book.lua 01 02 07   # those chapters
    pandoc lua doc/FieldGuide/reference/build_book.lua all        # every chapter
    pandoc lua doc/FieldGuide/reference/build_book.lua book       # the printed book

Needs nothing but pandoc 3.  Adapted from OODP_Lessons'
Course/reference/build_chapter.lua, which builds the C# course book, so the two
books share a palette, a box style, and a page layout.

A chapter's Word file goes to build/word, for Flint, which takes curriculum
documents best as .docx.  The book goes to build/book as Field_Guide_Draft.docx;
finish_book.ps1 lays it out in Word and makes the PDF.

Edit the Markdown and re-run.  Leave the generated files alone -- the next run
overwrites them, the reference .docx files included.
]]

local HERE = pandoc.path.directory(arg[0])
local GUIDE = pandoc.path.directory(HERE)
local CHAPTER_DIR = pandoc.path.join { GUIDE, "chapters" }
local BUILD = pandoc.path.join { GUIDE, "build" }

local BOOK_TITLE = "A Field Guide to the Robot"      -- a working title
local BOOK_TAGLINE = "The code on the page is the code on the robot."
local SERIES = "Winsor Robotics"

-- The parts, in book order.  Each gets a page of its own ahead of its first
-- chapter.  Chapter 0 opens the book and 98 and 99 close it, outside any part.
local PARTS = {
  { name = "Part 1 - Reading the Code", about = "Chapters 1 and 2", chapters = { "01", "02" } },
  { name = "Part 2 - Hardware and Sensing", about = "Chapters 3 to 5", chapters = { "03", "04", "05" } },
  { name = "Part 3 - The Robot Model", about = "Chapters 6 to 11", chapters = { "06", "07", "08", "09", "10", "11" } },
  { name = "Part 4 - Behavior Over Time", about = "Chapters 12 and 13", chapters = { "12", "13" } },
  { name = "Part 5 - Seeing the Field", about = "Chapters 14 and 15", chapters = { "14", "15" } },
  { name = "Part 6 - Building the Robot", about = "Chapters 16 to 20", chapters = { "16", "17", "18", "19", "20" } },
  { name = "Part 7 - Designing a Robot", about = "Chapters 21 and 22", chapters = { "21", "22" } },
}
local FRONT = { "00" }
local BACK = { "98", "99" }

local NBSP = "\u{A0}"

-- Smart typography stays off.  It would turn `--` into an en dash and straight
-- quotes into curly ones, and WRITING_COMMON_ERRORS.md entry 1 bans the
-- typographic dash.  The chapter's own characters go through unchanged.
local READER = "markdown-smart"

---------------------------------------------------------------------------
-- Finding a chapter
---------------------------------------------------------------------------

-- chapters/chapter_07_components.md is chapter "07".
local function source_of(key)
  for _, name in ipairs(pandoc.system.list_directory(CHAPTER_DIR)) do
    if name:match("^chapter_" .. key .. "_.*%.md$") then
      return pandoc.path.join { CHAPTER_DIR, name }
    end
  end
  return nil
end

local function part_of(key)
  for _, part in ipairs(PARTS) do
    for _, k in ipairs(part.chapters) do
      if k == key then return part end
    end
  end
  return nil
end

local function read_file(path)
  local f = io.open(path, "rb")
  if not f then return nil end
  local text = f:read("a")
  f:close()
  return text
end

---------------------------------------------------------------------------
-- The double space
---------------------------------------------------------------------------

-- Markdown collapses two spaces after a sentence into one.  Jason reads by
-- those spaces, so this pass keeps them:  each becomes a no-break space plus a
-- space, which survives pandoc into Word.  A sentence that ends a wrapped line
-- gets the no-break space at the end of the line.  Code fences and code spans
-- are left alone.

local function strip_prefix(line)
  return (line:gsub("^[>%s]*", ""))
end

-- A line of prose can run on into the next line.  These can't.
local function not_prose(body)
  return body == "" or body:match("^```") or body:match("^#") or body:match("^|")
      or body:match("^%-%-%-")
end

-- A line that opens a new block, so the line above it ends a paragraph.
local function starts_block(body)
  return not_prose(body) or body:match("^%- ") or body:match("^%d+%. ")
end

local function protect_spaces(md)
  local lines = {}
  for line in (md:gsub("\r\n", "\n") .. "\n"):gmatch("(.-)\n") do
    lines[#lines + 1] = line
  end

  local fenced = false
  for i, line in ipairs(lines) do
    local body = strip_prefix(line)
    if body:match("^```") then
      fenced = not fenced
    elseif not fenced then
      local spans = {}
      line = line:gsub("`[^`]*`", function(span)
        spans[#spans + 1] = span
        return "\0" .. #spans .. "\0"
      end)
      line = line:gsub("(%S)  %f[%S]", "%1" .. NBSP .. " ")

      local next_body = lines[i + 1] and strip_prefix(lines[i + 1]) or ""
      if not not_prose(body) and not starts_block(next_body)
          and line:match("[.?!:;][%*_%)\"']*$") then
        line = line .. NBSP
      end
      lines[i] = line:gsub("%z(%d+)%z", function(n) return spans[tonumber(n)] end)
    end
  end
  return table.concat(lines, "\n")
end

---------------------------------------------------------------------------
-- Title and body
---------------------------------------------------------------------------

local function trim_rules(text)
  text = text:gsub("^%s*%-%-%-%s*\n", ""):gsub("\n%s*%-%-%-%s*$", "")
  return (text:gsub("^%s+", ""):gsub("%s+$", ""))
end

-- Pulls the opening "# Title" off a chapter, and the italic line under it if
-- there is one, for the banner and the Word title.
local function take_title(piece)
  local title, after = piece:match("^#%s+([^\n]+)\n(.*)$")
  local subtitle, rest = after:match("^%s*%*([^%*\n][^\n]*)%*%s*\n(.*)$")
  if subtitle then return title, subtitle, trim_rules(rest) end
  return title, nil, trim_rules(after)
end

---------------------------------------------------------------------------
-- The boxes
---------------------------------------------------------------------------

-- Blockquotes that open with one of these bold labels become boxes.  The
-- colors are OODP's:  Aside blue, Try it in the Check green, Watch out red,
-- and the two question boxes purple.
local BOXES = {
  ["Aside"] = { fill = "F2F7FD", edge = "CFE0F3", accent = "2B6CB0" },
  ["Try it"] = { fill = "F1FBF5", edge = "C2E6D1", accent = "1D7A44" },
  ["Watch out"] = { fill = "FDF2F1", edge = "F0C9C4", accent = "B4382C" },
  ["Predict"] = { fill = "FAF4FE", edge = "E0CBF0", accent = "7B3FA0" },
  ["Argue it"] = { fill = "FAF4FE", edge = "E0CBF0", accent = "7B3FA0" },
}

local function label_of(block)
  if block.t ~= "Para" or not block.content[1] or block.content[1].t ~= "Strong" then
    return nil
  end
  return pandoc.utils.stringify(block.content[1]):match("^(.-)[%.:]")
end

local TEXT_WIDTH = 9360   -- US Letter, one-inch margins, in twentieths of a point

local function no_look()
  return '<w:tblLook w:val="0000" w:firstRow="0" w:lastRow="0" w:firstColumn="0" '
      .. 'w:lastColumn="0" w:noHBand="1" w:noVBand="1"/>'
end

-- Roughly how many lines a box will take on the page, counting a wrapped line
-- of prose at about 85 characters, plus a line of spacing per paragraph.
local function box_lines(content)
  local lines = 0
  for _, block in ipairs(content) do
    if block.t == "CodeBlock" then
      for line in (block.text .. "\n"):gmatch("(.-)\n") do
        lines = lines + math.max(1, math.ceil(#line / 90))
      end
      lines = lines + 1
    elseif block.t == "Table" then
      lines = lines + 2 * (#block.bodies[1].body + 1)
    else
      lines = lines + math.ceil(#pandoc.utils.stringify(block) / 85) + 1
    end
  end
  return lines
end

-- A box is a table.  A box that fits on a page is one row that can't split,
-- so it moves to the next page whole and never breaks across two.  A longer
-- box breaks between its parts:  each code block, and each table, gets a row
-- of its own that can't split, and the prose between them shares a row.
-- Borders are drawn per cell, top on the first row and bottom on the last, so
-- the rows read as one box.
local SHORT_BOX = 44   -- lines;  about a page
local function word_box(c, content)
  local rows = {}
  if box_lines(content) <= SHORT_BOX then
    rows[1] = { code = true, blocks = pandoc.List(content) }
  else
    for _, block in ipairs(content) do
      local last = rows[#rows]
      local whole = block.t == "CodeBlock" or block.t == "Table"   -- kept on one page
      if whole or not last or last.code then
        rows[#rows + 1] = { code = whole, blocks = pandoc.List { block } }
      else
        last.blocks:insert(block)
      end
    end
  end

  local edge = ('w:val="single" w:sz="4" w:space="0" w:color="%s"'):format(c.edge)
  local out = pandoc.List { pandoc.RawBlock("openxml", table.concat {
    '<w:tbl><w:tblPr><w:tblW w:w="', TEXT_WIDTH, '" w:type="dxa"/>',
    '<w:tblInd w:w="92" w:type="dxa"/><w:tblLayout w:type="fixed"/>',
    '<w:tblBorders><w:top w:val="nil"/><w:left w:val="nil"/><w:bottom w:val="nil"/>',
    '<w:right w:val="nil"/><w:insideH w:val="nil"/><w:insideV w:val="nil"/></w:tblBorders>',
    '<w:tblCellMar><w:top w:w="0" w:type="dxa"/><w:left w:w="200" w:type="dxa"/>',
    '<w:bottom w:w="0" w:type="dxa"/><w:right w:w="200" w:type="dxa"/></w:tblCellMar>',
    no_look(), '</w:tblPr><w:tblGrid><w:gridCol w:w="', TEXT_WIDTH, '"/></w:tblGrid>',
  }) }
  for k, row in ipairs(rows) do
    local first, last = k == 1, k == #rows
    out:insert(pandoc.RawBlock("openxml", table.concat {
      '<w:tr>', row.code and '<w:trPr><w:cantSplit/></w:trPr>' or '',
      '<w:tc><w:tcPr><w:tcW w:w="', TEXT_WIDTH, '" w:type="dxa"/><w:tcBorders>',
      first and ('<w:top ' .. edge .. '/>') or '<w:top w:val="nil"/>',
      '<w:left w:val="single" w:sz="36" w:space="0" w:color="', c.accent, '"/>',
      last and ('<w:bottom ' .. edge .. '/>') or '<w:bottom w:val="nil"/>',
      '<w:right ', edge, '/></w:tcBorders>',
      '<w:shd w:val="clear" w:color="auto" w:fill="', c.fill, '"/>',
      '<w:tcMar><w:top w:w="', first and 60 or 0, '" w:type="dxa"/>',
      '<w:bottom w:w="', last and 60 or 0, '" w:type="dxa"/></w:tcMar></w:tcPr>',
    }))
    out:extend(row.blocks)
    -- A cell has to end on a paragraph, and a table is no paragraph.
    local tail = row.blocks[#row.blocks].t == "Table"
      and '<w:p><w:pPr><w:spacing w:before="0" w:after="0" w:line="120" w:lineRule="exact"/></w:pPr></w:p>'
      or ""
    out:insert(pandoc.RawBlock("openxml", tail .. "</w:tc></w:tr>"))
  end
  -- Two tables in a row would run together in Word, so a thin paragraph
  -- follows each box.
  out:insert(pandoc.RawBlock("openxml", '</w:tbl><w:p><w:pPr><w:spacing w:before="0" '
    .. 'w:after="0" w:line="120" w:lineRule="exact"/></w:pPr></w:p>'))
  return out
end

-- An empty paragraph, a hair tall, that starts a new page.  The heading after
-- it lands at the top.
local PAGE_BREAK = pandoc.RawBlock("openxml", '<w:p><w:pPr><w:pageBreakBefore/>'
  .. '<w:spacing w:before="0" w:after="0" w:line="20" w:lineRule="exact"/></w:pPr></w:p>')

-- A numbered entry, "## 7.3  ...".
local function is_entry(block)
  return block.t == "Header" and block.level == 2
      and pandoc.utils.stringify(block):match("^%d+%.%d+") ~= nil
end

local function box_filter()
  return {
    -- Each numbered entry starts a new page;  finish_book.ps1 takes the break
    -- back out where the page is better for it.  Every other heading drops a
    -- level, so "Chapter summary" and "Where to go next" sit below the
    -- entries, and the contents lists entries and nothing else.
    Blocks = function(blocks)
      local out = pandoc.Blocks {}
      for k, block in ipairs(blocks) do
        local after = blocks[k + 1]
        if block.t == "HorizontalRule" and after and after.t == "Header" then
          -- dropped:  a rule above a heading would sit alone at a page's foot
        else
          if is_entry(block) then
            out:insert(PAGE_BREAK)
          elseif block.t == "Header" and block.level >= 2 then
            block.level = block.level + 1
          end
          -- A paragraph that leads into a box, a code block, or a table keeps
          -- to the same page as it.  The boxes are raw tables by now.
          local leads_in = after and (after.t == "CodeBlock" or after.t == "Table"
            or (after.t == "RawBlock" and after.text:sub(1, 7) == "<w:tbl>"))
          if block.t == "Para" and leads_in then
            out:insert(pandoc.Div({ block }, { ["custom-style"] = "Keep With Next" }))
          else
            out:insert(block)
          end
        end
      end
      return out
    end,
    -- [*]{.iceberg}:  one big asterisk, right after a claim the book has
    -- simplified on purpose.  Left unexplained.
    Span = function(span)
      if not span.classes:includes("iceberg") then return nil end
      return pandoc.RawInline("openxml", '<w:r><w:rPr><w:b/><w:color w:val="1F4E79"/>'
        .. '<w:position w:val="-2"/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr><w:t>*</w:t></w:r>')
    end,
    Header = function(header)
      return header:walk {
        Code = function(code)
          return pandoc.Span(code.text, { ["custom-style"] = "Heading Code" })
        end,
      }
    end,
    BlockQuote = function(quote)
      local colours = BOXES[label_of(quote.content[1])]
      if not colours then return nil end
      return word_box(colours, quote.content)
    end,
  }
end

-- Box filters run before the Blocks filter sees the list, so boxes are raw
-- tables by the time "leads into a box" gets checked.
local function apply_filters(doc)
  local f = box_filter()
  doc = doc:walk { BlockQuote = f.BlockQuote, Span = f.Span, Header = f.Header }
  return doc:walk { Blocks = f.Blocks }
end

---------------------------------------------------------------------------
-- Word
---------------------------------------------------------------------------

-- Pandoc's default reference.docx, restyled:  Calibri, Consolas, accent
-- 1F4E79, US Letter, code in shaded blocks, and tables with borders.
local STYLES = {
  Title = [[<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/>
<w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:qFormat/>
<w:pPr><w:spacing w:before="0" w:after="60"/></w:pPr>
<w:rPr><w:b/><w:color w:val="1F4E79"/><w:sz w:val="48"/><w:szCs w:val="48"/></w:rPr></w:style>]],
  Subtitle = [[<w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/>
<w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:qFormat/>
<w:pPr><w:spacing w:before="0" w:after="60"/></w:pPr>
<w:rPr><w:i/><w:color w:val="2D3B47"/><w:sz w:val="28"/><w:szCs w:val="28"/></w:rPr></w:style>]],
  Date = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="Date"><w:name w:val="Date"/>
<w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:qFormat/>
<w:pPr><w:pBdr><w:bottom w:val="single" w:sz="8" w:space="6" w:color="1F4E79"/></w:pBdr>
<w:spacing w:before="0" w:after="240"/></w:pPr>
<w:rPr><w:b/><w:caps/><w:color w:val="5B6B7A"/><w:spacing w:val="10"/><w:sz w:val="18"/><w:szCs w:val="18"/></w:rPr></w:style>]],
  SourceCode = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="SourceCode"><w:name w:val="Source Code"/>
<w:basedOn w:val="Normal"/><w:link w:val="VerbatimChar"/>
<w:pPr><w:keepLines/><w:pBdr><w:top w:val="single" w:sz="4" w:space="4" w:color="C9D8E8"/>
<w:left w:val="single" w:sz="4" w:space="4" w:color="C9D8E8"/>
<w:bottom w:val="single" w:sz="4" w:space="4" w:color="C9D8E8"/>
<w:right w:val="single" w:sz="4" w:space="4" w:color="C9D8E8"/></w:pBdr>
<w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/>
<w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/>
<w:ind w:left="100" w:right="100"/></w:pPr></w:style>]],
  VerbatimChar = [[<w:style w:type="character" w:customStyle="1" w:styleId="VerbatimChar">
<w:name w:val="Verbatim Char"/><w:basedOn w:val="BodyTextChar"/>
<w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:cs="Consolas"/><w:color w:val="1A4D80"/>
<w:sz w:val="19"/><w:szCs w:val="19"/></w:rPr></w:style>]],
  -- Code in a heading takes the heading's size, where Verbatim Char would shrink it.
  HeadingCode = [[<w:style w:type="character" w:customStyle="1" w:styleId="HeadingCode">
<w:name w:val="Heading Code"/><w:basedOn w:val="DefaultParagraphFont"/>
<w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:cs="Consolas"/></w:rPr></w:style>]],
  -- A paragraph that introduces what comes next, kept on its page.
  KeepWithNext = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="KeepWithNext">
<w:name w:val="Keep With Next"/><w:basedOn w:val="BodyText"/>
<w:pPr><w:keepNext/></w:pPr></w:style>]],
  -- The compiled book's title page.
  BookTitle = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="BookTitle">
<w:name w:val="Book Title"/><w:basedOn w:val="Normal"/>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="240" w:after="120"/></w:pPr>
<w:rPr><w:b/><w:color w:val="1F4E79"/><w:sz w:val="64"/><w:szCs w:val="64"/></w:rPr></w:style>]],
  BookSubtitle = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="BookSubtitle">
<w:name w:val="Book Subtitle"/><w:basedOn w:val="Normal"/>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="0" w:after="480"/></w:pPr>
<w:rPr><w:i/><w:color w:val="2D3B47"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:style>]],
  BookByline = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="BookByline">
<w:name w:val="Book Byline"/><w:basedOn w:val="Normal"/>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="0" w:after="60"/></w:pPr>
<w:rPr><w:color w:val="5B6B7A"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:style>]],
  BookCover = [[<w:style w:type="paragraph" w:customStyle="1" w:styleId="BookCover">
<w:name w:val="Book Cover"/><w:basedOn w:val="Normal"/>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="720" w:after="360"/></w:pPr></w:style>]],
  Table = [[<w:style w:type="table" w:default="1" w:styleId="Table"><w:name w:val="Table"/>
<w:basedOn w:val="TableNormal"/><w:qFormat/>
<w:pPr><w:spacing w:before="40" w:after="40"/></w:pPr>
<w:tblPr><w:tblInd w:w="0" w:type="dxa"/><w:tblBorders>
<w:top w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/>
<w:left w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/>
<w:bottom w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/>
<w:right w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/>
<w:insideH w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/>
<w:insideV w:val="single" w:sz="4" w:space="0" w:color="C9D8E8"/></w:tblBorders>
<w:tblCellMar><w:top w:w="60" w:type="dxa"/><w:left w:w="108" w:type="dxa"/>
<w:bottom w:w="60" w:type="dxa"/><w:right w:w="108" w:type="dxa"/></w:tblCellMar></w:tblPr>
<w:tblStylePr w:type="firstRow"><w:rPr><w:b/><w:color w:val="3D556F"/></w:rPr>
<w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="E5EDF5"/></w:tcPr></w:tblStylePr></w:style>]],
}

local function restyle(styles)
  styles = styles:gsub("<w:rFonts[^>]-Theme[^>]*/>",
    '<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Calibri" w:cs="Calibri" />')
  styles = styles:gsub('<w:color w:val="0F4761"[^>]*/>', '<w:color w:val="1F4E79" />')
  -- Each part and each chapter starts a new page.
  styles = styles:gsub('(w:styleId="Heading1">.-<w:pPr>)', '%1<w:pageBreakBefore/>', 1)
  styles = styles:gsub('<w:sz w:val="24" />', '<w:sz w:val="22" />', 1)
  styles = styles:gsub('<w:szCs w:val="24" />', '<w:szCs w:val="22" />', 1)
  for id, xml in pairs(STYLES) do
    local pattern = '<w:style [^>]-w:styleId="' .. id .. '".-</w:style>'
    styles = styles:gsub(pattern, "", 1)
    styles = styles:gsub("</w:styles>", function() return xml .. "\n</w:styles>" end)
  end
  return styles
end

local function make_reference_doc(path, left_margin)
  local left = left_margin or 1440
  local bytes = pandoc.pipe("pandoc", { "--print-default-data-file", "reference.docx" }, "")
  local archive = pandoc.zip.Archive(bytes)
  local entries = pandoc.List()
  for _, entry in ipairs(archive.entries) do
    local contents = entry:contents()
    if entry.path == "word/styles.xml" then
      contents = restyle(contents)
    elseif entry.path == "word/document.xml" then
      contents = contents:gsub("<w:sectPr>", '<w:sectPr><w:pgSz w:w="12240" w:h="15840"/>'
        .. '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="' .. left .. '" '
        .. 'w:header="720" w:footer="720" w:gutter="0"/>', 1)
    end
    entries:insert(pandoc.zip.Entry(entry.path, contents))
  end
  local f = assert(io.open(path, "wb"))
  f:write(pandoc.zip.Archive(entries):bytestring())
  f:close()
end

-- Every paragraph in a table row keeps with the next row.  The paragraph's
-- style has to come first inside its properties, so keepNext goes after it.
local function keep_with_next(row)
  row = row:gsub('<w:p><w:pPr><w:pStyle ([^>]-)/>', '<w:p><w:pPr><w:pStyle %1/><w:keepNext />')
  row = row:gsub('<w:p><w:pPr>(<w:%a+)', function(tag)
    if tag == "<w:pStyle" or tag == "<w:keepNext" then return nil end
    return '<w:p><w:pPr><w:keepNext />' .. tag
  end)
  return (row:gsub('<w:p>(<w:[rh])', '<w:p><w:pPr><w:keepNext /></w:pPr>%1'))
end

-- pandoc's tables, the ones styled "Table", keep to one page:  no row splits,
-- and every row but the last keeps with the next.  The boxes are tables too,
-- and they're free to break between their rows.  pandoc's tables hold no
-- tables of their own, so the first </w:tbl> after one starts is its end.
local function keep_tables_whole(xml)
  return (xml:gsub('<w:tbl><w:tblPr><w:tblStyle w:val="Table" />.-</w:tbl>', function(tbl)
    local count = select(2, tbl:gsub("<w:tr>", ""))
    local k = 0
    return (tbl:gsub("<w:tr>(.-)</w:tr>", function(row)
      k = k + 1
      if row:sub(1, 8) == "<w:trPr>" then
        row = "<w:trPr><w:cantSplit />" .. row:sub(9)
      else
        row = "<w:trPr><w:cantSplit /></w:trPr>" .. row
      end
      if k < count then row = keep_with_next(row) end
      return "<w:tr>" .. row .. "</w:tr>"
    end))
  end))
end

local function finish_docx(bytes)
  local archive = pandoc.zip.Archive(bytes)
  local entries = pandoc.List()
  for _, entry in ipairs(archive.entries) do
    local contents = entry:contents()
    if entry.path == "word/document.xml" then contents = keep_tables_whole(contents) end
    entries:insert(pandoc.zip.Entry(entry.path, contents))
  end
  return pandoc.zip.Archive(entries):bytestring()
end

local function inlines_of(text)
  return pandoc.Inlines(pandoc.read(text, READER).blocks[1].content)
end

---------------------------------------------------------------------------
-- One chapter, for Flint
---------------------------------------------------------------------------

local function word_name(key)
  if key == "00" then return "FieldGuide_00_How_to_Use.docx" end
  if key == "98" then return "FieldGuide_98_Glossary.docx" end
  if key == "99" then return "FieldGuide_99_Postscript.docx" end
  return ("FieldGuide_Chapter%s.docx"):format(key)
end

local function build(key)
  local source = assert(source_of(key), "no chapters/chapter_" .. key .. "_*.md")
  local md = protect_spaces(read_file(source))
  local title, subtitle, body = take_title(trim_rules(md))

  local reference = pandoc.path.join { HERE, "chapter-reference.docx" }
  make_reference_doc(reference)
  local out_dir = pandoc.path.join { BUILD, "word" }
  pandoc.system.make_directory(out_dir, true)

  local part = part_of(key)
  local doc = apply_filters(pandoc.read(body, READER))
  doc.meta.title = inlines_of(title)
  if subtitle then doc.meta.subtitle = inlines_of(subtitle) end
  doc.meta.date = part and (part.name .. "  ·  " .. BOOK_TITLE) or BOOK_TITLE

  local path = pandoc.path.join { out_dir, word_name(key) }
  local f = assert(io.open(path, "wb"))
  f:write(finish_docx(pandoc.write(doc, "docx", { reference_doc = reference })))
  f:close()
  print("wrote " .. path)
end

---------------------------------------------------------------------------
-- The whole book, in one Word file, for printing
---------------------------------------------------------------------------

-- `book` puts every chapter, whole, into one Word file:  a title page, the
-- contents, then the parts in order, each on a page of its own ahead of its
-- chapters.  Every page gets a 1.5-inch left margin, so a three-hole punch
-- clears the text.  finish_book.ps1 mirrors the margins for double-sided
-- printing, fills in the contents and page numbers, and makes the PDF.

local function styled(style, inlines)
  return pandoc.Div({ pandoc.Para(inlines) }, { ["custom-style"] = style })
end

local function book_order()
  local order = {}
  for _, key in ipairs(FRONT) do order[#order + 1] = key end
  for _, part in ipairs(PARTS) do
    order[#order + 1] = part
    for _, key in ipairs(part.chapters) do order[#order + 1] = key end
  end
  for _, key in ipairs(BACK) do order[#order + 1] = key end
  return order
end

local function compile_book()
  TEXT_WIDTH = 8640   -- 8.5 inches, less 1.5 and 1 for the margins
  local out_dir = pandoc.path.join { BUILD, "book" }
  pandoc.system.make_directory(out_dir, true)
  local reference = pandoc.path.join { HERE, "book-reference.docx" }
  make_reference_doc(reference, 2160)

  local blocks = pandoc.Blocks {}
  local cover = pandoc.path.join { out_dir, "cover.png" }
  if read_file(cover) then
    blocks:insert(styled("Book Cover", { pandoc.Image({}, cover, "", { width = "3.4in" }) }))
  end
  blocks:insert(styled("Book Title", inlines_of(BOOK_TITLE)))
  blocks:insert(styled("Book Subtitle", { pandoc.Str((BOOK_TAGLINE:gsub("  ", NBSP .. " "))) }))
  blocks:insert(styled("Book Byline", { pandoc.Str(SERIES) }))
  blocks:insert(styled("Book Byline", { pandoc.Str("Jason Cox") }))
  blocks:insert(styled("Book Byline", { pandoc.Str("written with Claude, an AI writing assistant") }))
  blocks:insert(styled("Book Byline", { pandoc.Str("Draft, " .. os.date("%B %Y")) }))
  blocks:insert(PAGE_BREAK)
  blocks:insert(styled("Book Title", { pandoc.Str("Contents") }))
  blocks:insert(pandoc.RawBlock("openxml", '<w:p><w:r><w:fldChar w:fldCharType="begin"/></w:r>'
    .. '<w:r><w:instrText xml:space="preserve"> TOC \\o "1-2" \\h \\z \\u </w:instrText></w:r>'
    .. '<w:r><w:fldChar w:fldCharType="separate"/></w:r>'
    .. '<w:r><w:t>Word fills in the contents.</w:t></w:r>'
    .. '<w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>'))

  -- Where each chapter starts, in book order, for finish_book.ps1:  it cuts
  -- each chapter's PDF out of the finished book, so the two agree page for
  -- page.  A part page is a boundary with no file of its own.
  local starts = {}
  for _, item in ipairs(book_order()) do
    if type(item) == "table" then
      starts[#starts + 1] = { "", item.name }
      blocks:insert(pandoc.Header(1, { pandoc.Str(item.name) }))
      blocks:insert(styled("Book Byline", { pandoc.Str(item.about) }))
    else
      local source = source_of(item)
      if not source then
        print("skipped chapter " .. item .. ":  no file yet")
      else
        local md = protect_spaces(read_file(source))
        local title, subtitle, body = take_title(trim_rules(md))
        starts[#starts + 1] = { (word_name(item):gsub("%.docx$", ".pdf")),
          pandoc.utils.stringify(inlines_of(title)) }

        blocks:insert(pandoc.Header(1, inlines_of(title)))
        if subtitle then blocks:insert(styled("Subtitle", inlines_of(subtitle))) end

        -- The chapter's title page:  its picture (make_art.ps1 draws them),
        -- and its opening from book/:  a line of prose, then a moment quoted
        -- from the chapter.  The opening goes in unlabeled.  The chapter
        -- starts on the page after.
        local art = pandoc.path.join { out_dir, "art", "ch" .. item .. ".png" }
        local have_art = read_file(art) ~= nil
        if have_art then
          blocks:insert(styled("Book Cover", { pandoc.Image({}, art, "", { width = "5.6in" }) }))
        end
        local opening = read_file(pandoc.path.join { GUIDE, "book", "opening_" .. item .. ".md" })
        if opening then
          -- Two code blocks in a row would merge into one box in Word, so a
          -- thin paragraph goes between them:  the code, then what it printed.
          local previous
          for _, block in ipairs(pandoc.read(protect_spaces(opening), READER).blocks) do
            if previous == "CodeBlock" and block.t == "CodeBlock" then
              blocks:insert(pandoc.RawBlock("openxml", '<w:p><w:pPr><w:spacing w:before="0" '
                .. 'w:after="0" w:line="160" w:lineRule="exact"/></w:pPr></w:p>'))
            end
            if block.t == "Para" then
              blocks:insert(pandoc.Div({ block }, { ["custom-style"] = "Book Byline" }))
            else
              blocks:insert(block)
            end
            previous = block.t
          end
        end
        if have_art or opening then blocks:insert(PAGE_BREAK) end

        blocks:extend(apply_filters(pandoc.read(body, READER)).blocks)
      end
    end
  end

  local path = pandoc.path.join { out_dir, "Field_Guide_Draft.docx" }
  local f = assert(io.open(path, "wb"))
  f:write(finish_docx(pandoc.write(pandoc.Pandoc(blocks), "docx", { reference_doc = reference })))
  f:close()
  local list = assert(io.open(pandoc.path.join { out_dir, "chapters.tsv" }, "wb"))
  for _, s in ipairs(starts) do list:write(s[1], "\t", s[2], "\n") end
  list:close()
  TEXT_WIDTH = 9360
  print("wrote " .. path)
end

---------------------------------------------------------------------------

local function every_chapter()
  local keys = {}
  for _, item in ipairs(book_order()) do
    if type(item) == "string" and source_of(item) then keys[#keys + 1] = item end
  end
  return keys
end

for _, key in ipairs(#arg > 0 and arg or { "all" }) do
  if key == "book" then compile_book()
  elseif key == "all" then for _, k in ipairs(every_chapter()) do build(k) end
  else build(key) end
end
