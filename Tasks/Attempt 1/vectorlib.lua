-- Vector module
-- @module Vector
Vector = {x = 0, y = 0, z = 0}
Vector.__index = Vector

-- create a new vector
-- @param x the x value
-- @param y the y value
-- @return a new vector
function Vector:new(x, y, z)
    local o = {}
    setmetatable(o, {__index = self})   
    self.__index = self
    o.x = x or 0
    o.y = y or 0
    o.z = z or 0
    return o
end

-- check whether a vector or not
-- @param o object
-- @return true/false
function Vector:IsVector(o)
    return o ~= nil and type(o.x) == 'number' and type(o.y) == 'number' and type(o.z) == 'number'
end

-- check whether two vectors are equal
-- @param a first vector
-- @param b second vector
-- @return true/false
function Vector:Eq(a,b)
    assert(Vector:IsVector(a) and Vector:IsVector(b), "Incorrect argument types: expected <Vector> and <Vector>)")
    return a.x == b.x and a.y == b.y and a.z == b.z
 end

-- generate a random 2D vector
-- @return a new vector
 function Vector:Random2D ()
    x = math.random()
    y = math.random()
    return Vector:new(x, y)
 end

-- add two vectors and return a new vector
-- @param a first vector
-- @param b second vector
-- @return a new vector
function Vector:Add(a,b)
    assert(Vector:IsVector(a) and Vector:IsVector(b), "Incorrect argument types: expected <Vector> and <Vector>")
    return Vector:new(a.x + b.x, a.y + b.y, a.z + b.z)
end

-- add a second vector to the first vector
-- @param b second vector
-- @return the first vector
function Vector:add(b)
    assert(Vector:IsVector(b), "Incorrect argument type: expected <Vector>")
    self.x = self.x + b.x
    self.y = self.y + b.y
    self.z = self.z + b.z
    return self
end

-- subtract second vector from the first vector and return a new vector
-- @param a first vector
-- @param b second vector
-- @return a new vector
function Vector:Sub(a,b)
    assert(Vector:IsVector(a) and Vector:IsVector(b), "Incorrect argument types: expected <Vector> and <Vector>")
    return Vector:new(a.x - b.x, a.y - b.y, a.z - b.z)
end

-- subtract second vector from the first vector
-- @param b second vector
-- @return the first vector
function Vector:sub(b)
    assert(Vector:IsVector(b), "Incorrect argument type: expected <Vector>")
    self.x = self.x - b.x
    self.y = self.y - b.y
    self.z = self.z - b.z
    return self
end

-- multiply a vector by a scaler and return a new vector
-- @param a vector
-- @param c scaler
-- @return a new vector
function Vector:Mul(a,c)
    assert(Vector:IsVector(a) and type(c) == 'number', "Incorrect argument types: expected <Vector> and <number>")
    return Vector:new(a.x * c, a.y * c, a.z * c)
end

-- multiply a vector by a scaler
-- @param c scaler
-- @return the vector
function Vector:mul(c)
    assert(type(c) == 'number', "Incorrect argument type: expected <number>")
    self.x = self.x * c
    self.y = self.y * c
    self.z = self.z * c
    return self
end

-- divide a vector by a scaler and return a new vector
-- @param a vector
-- @param c scaler
-- @return a new vector
function Vector:Div(a,c)
    assert(Vector:IsVector(a) and type(c) == 'number', "Incorrect argument types: expected <Vector> and <number>")
    return Vector:new(a.x / c, a.y / c, a.z / c)
end

-- divide a vector by a scaler
-- @param c scaler
-- @return the vector
function Vector:div(c)
    assert(type(c) == 'number', "Incorrect argument type: expected <number>")
    self.x = self.x / c
    self.y = self.y / c
    self.z = self.z / c
    return self
end

-- calculate the dot product of two vectors
-- @param b second vector
-- @return the first vector
function Vector:dot(b)
    assert(Vector:IsVector(b), "Incorrect argument type: expected <Vector>")
    return self.x * b.x + self.y * b.y + self.z * b.z
end

-- calculate the magnitude of a vector
-- @return the magnitude
function Vector:mag()
    return math.sqrt(self.x^2 + self.y^2 + self.z^2)
end

-- normalize a vector
-- @return the vector
function Vector:normalize()
    local len = self:mag()
    assert(len ~= 0, "Cannot set magnitude when direction is unknown")
    if len~=0 then
      self.x = self.x / len
      self.y = self.y / len
      self.z = self.z / len
    end
    return self
end

-- set/update magnitude of a vector
-- @param mag magnitude
-- @return the vector
function Vector:setMag(mag)
    self = self:normalize():mul(mag)
    return self
end

-- limit/cap the magnitude of a vector
-- @param max maximum magnitude
-- @return the vector
function Vector:limit(max)
  local len = self:mag()
  if len > max then
    self:setMag(max)
  end
  return self
end