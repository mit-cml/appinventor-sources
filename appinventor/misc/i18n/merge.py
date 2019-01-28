

def explode_at_capitals(text):
    exploded = ['']
    index = 0
    multichar = False
    for i in text:
        if i == '\n':
            exploded[index] += i
        elif 'A' <= i <= 'Z':
            if len(exploded[index]) == 0:
                # New string
                exploded[index] += i
                multichar = False
            elif 'A' <= exploded[index][-1] <= 'Z':
                # Continuation of ALL CAPS
                exploded[index] += i
                multichar = True
            else:
                # Start of a new string
                exploded.append(i)
                index += 1
                multichar = False
        elif multichar:
            # Finished APP CAPS continuation
            exploded.append(exploded[index][-1] + i)
            exploded[index] = exploded[index][:-1]
            index += 1
            multichar = False
        else:
            exploded[index] += i
    return ' '.join(exploded)


def write_with_prefix(input, output, prefix):
    continuation = False
    for line in input:
        if len(line) <= 2:
            output.write(line)
        elif line[0] == '#':
            output.write(line)
        else:
            parts = line.split('=', 2)
            if parts[0].endswith('Properties') or parts[0].endswith('Methods') or parts[0].endswith('Events') or parts[0].endswith('Params'):
                output.write(prefix)
                output.write(parts[0])
                output.write('=')
                output.write(explode_at_capitals(parts[1]))
                continuation = line.trim()[-1] == '\\'
            elif 'HELPURL' in parts[0]:
                pass  # Ignore help URLs, which aren't translated
            elif continuation:
                output.write(line)
                continuation = line.trim()[-1] == '\\'
            else:
                output.write(prefix)
                output.write(line)
                continuation = line.trim()[-1] == '\\'

with open('en.properties', 'w', encoding='utf-8') as output:
    with open('appengine/build/extras/ode/com.google.appinventor.client.OdeMessages_en.properties') as input:
        write_with_prefix(input, output, "appengine.")
    with open('messages.properties') as input:
        write_with_prefix(input, output, "blockseditor.")
