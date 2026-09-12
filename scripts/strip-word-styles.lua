-- Word-exported HTML wraps nearly every phrase in style-only spans and divs.
-- Preserve their semantic contents without carrying those presentation wrappers
-- into the GitHub-flavored Markdown edition.
function Span(element)
  if element.identifier ~= '' and #element.content == 0 then
    return pandoc.RawInline('markdown', '<a id="' .. element.identifier .. '"></a>')
  end
  return element.content
end

function Div(element)
  return element.content
end
